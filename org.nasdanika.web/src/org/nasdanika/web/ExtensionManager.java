package org.nasdanika.web;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.net.URL;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.regex.Pattern;

import javax.servlet.http.HttpServletResponse;

import org.eclipse.core.runtime.IConfigurationElement;
import org.eclipse.core.runtime.IContributor;
import org.eclipse.core.runtime.Platform;
import org.nasdanika.core.AuthorizationProvider;
import org.nasdanika.core.Context;
import org.nasdanika.core.Converter;
import org.nasdanika.core.InstanceMethodCommand;
import org.nasdanika.core.MethodCommand;
import org.nasdanika.html.HTMLFactory;
import org.nasdanika.html.impl.DefaultHTMLFactory;
import org.nasdanika.web.RouteDescriptor.RouteType;
import org.osgi.framework.Bundle;
import org.osgi.framework.BundleContext;
import org.osgi.framework.Constants;
import org.osgi.framework.FrameworkUtil;
import org.osgi.framework.ServiceReference;
import org.osgi.util.tracker.ServiceTracker;

/**
 * Helper class for resolving and caching extensions and references.
 * @author Pavel
 *
 */
public class ExtensionManager implements AutoCloseable {
	
	private ServiceTracker<Route, Route> routeServiceTracker;
	private BundleContext bundleContext;
	private HTMLFactory htmlFactory;
	
	public ExtensionManager(BundleContext context, String routeServiceFilter, String htmlFactoryName) throws Exception {
		// TODO - converter profiles map: class name -> profile.
		if (context==null) {
			context = FrameworkUtil.getBundle(Route.class).getBundleContext();
		}
		// TODO - bundle is still null???
		this.bundleContext = context;
		if (routeServiceFilter==null || routeServiceFilter.trim().length()==0) {
			routeServiceTracker = new ServiceTracker<>(context, Route.class.getName(), null);
		} else {
			String rootRouteServiceFilter = "(&(" + Constants.OBJECTCLASS + "=" + Route.class.getName() + ")"+routeServiceFilter+")";
			routeServiceTracker = new ServiceTracker<>(context, context.createFilter(rootRouteServiceFilter), null);
		}
		routeServiceTracker.open();
		
		IConfigurationElement[] actionConfigurationElements = Platform.getExtensionRegistry().getConfigurationElementsFor(HTML_FACTORY_ID);
		for (IConfigurationElement ce: actionConfigurationElements) {
			if ("default_html_factory".equals(ce.getName())) {
				if (htmlFactoryName==null || htmlFactoryName.equals("default")) {
					DefaultHTMLFactory defaultHTMLFactory = new DefaultHTMLFactory();
					for (IConfigurationElement s: ce.getChildren("script")) {
						defaultHTMLFactory.getScripts().add(s.getValue());
					}
					for (IConfigurationElement s: ce.getChildren("stylesheet")) {
						defaultHTMLFactory.getStylesheets().add(s.getValue());
					}
					this.htmlFactory = defaultHTMLFactory;
					break;
				}
			} else if ("html_factory".equals(ce.getName())) {
				if (htmlFactoryName==null || htmlFactoryName.equals(ce.getAttribute("name"))) {
					this.htmlFactory = (HTMLFactory) ce.createExecutableExtension("class");
					injectProperties(ce, htmlFactory);
					break;
				}
			}					
		}		
	}
			
	public static final String HTML_FACTORY_ID = "org.nasdanika.web.html_factory";			
	public static final String ROUTE_ID = "org.nasdanika.web.route";			
	public static final String CONVERT_ID = "org.nasdanika.core.convert";				
	private static final String SECURITY_ID = "org.nasdanika.core.security";
	
	private Converter<Object, Object, WebContext> converter;
	
	protected static class ConverterServiceEntry implements Converter<Object,Object, WebContext> {
		
		private ServiceTracker<Converter<Object, Object, WebContext>, Converter<Object, Object, WebContext>> serviceTracker;		
		
		public ConverterServiceEntry(String filter) throws Exception {
			BundleContext context = FrameworkUtil.getBundle(ExtensionManager.class).getBundleContext();
			if (filter==null || filter.trim().length()==0) {
				this.serviceTracker = new ServiceTracker<Converter<Object, Object, WebContext>, Converter<Object, Object, WebContext>>(context, Converter.class.getName(), null);				
			} else {
				filter = "(&(" + Constants.OBJECTCLASS + "=" + Converter.class.getName() + ")"+filter+")";
				this.serviceTracker = new ServiceTracker<Converter<Object, Object, WebContext>, Converter<Object, Object, WebContext>>(context, context.createFilter(filter), null);
			}
			this.serviceTracker.open();
		}

		@Override
		public void close() throws Exception {
			serviceTracker.close();			
		}

		@Override
		public Object convert(Object source, Class<Object> target, WebContext context) throws Exception {
			// TODO - iterate over the getTracked(), match profiles.
			for (Object c: serviceTracker.getServices()) {
				@SuppressWarnings("unchecked")
				Object ret = ((Converter<Object,Object, WebContext>) c).convert(source, target, context);
				if (ret!=null) {
					return ret;
				}
			}
			return null;
		}
		
	}
	
	public synchronized Converter<Object, Object, WebContext> getConverter() throws Exception {
		if (converter==null) {
			class ConverterEntry implements Comparable<ConverterEntry> {
				
				public ConverterEntry(Converter<Object,Object,WebContext> converter) {
					this.converter = converter;
				}
				
				int priority;
				
				Class<?> source;
				Class<?> target;
				
				Converter<Object, Object, WebContext> converter;

				@Override
				public int compareTo(ConverterEntry o) {
					if (source.isAssignableFrom(o.source) && !o.source.isAssignableFrom(source)) {
						return 1; // o is more specific.
					}
					if (o.priority != priority) {
						return o.priority - priority;
					}
					if (target.isAssignableFrom(o.target) && !o.target.isAssignableFrom(target)) {
						return -1; // o is more specific
					}
					return 0;
				}
				
			}
			final List<ConverterEntry> ceList = new ArrayList<>();
			IConfigurationElement[] actionConfigurationElements = Platform.getExtensionRegistry().getConfigurationElementsFor(CONVERT_ID);
			for (IConfigurationElement ce: actionConfigurationElements) {
				if ("converter".equals(ce.getName())) {					
					@SuppressWarnings("unchecked")
					ConverterEntry cEntry = new ConverterEntry((Converter<Object, Object, WebContext>) ce.createExecutableExtension("class"));
					
					String priorityStr = ce.getAttribute("priority");
					if (!isBlank(priorityStr)) {
						cEntry.priority = Integer.parseInt(priorityStr);
					}
					
					IContributor contributor = ce.getContributor();		
					Bundle bundle = Platform.getBundle(contributor.getName());
					cEntry.source = (Class<?>) bundle.loadClass(ce.getAttribute("source").trim());
					cEntry.target = (Class<?>) bundle.loadClass(ce.getAttribute("target").trim());
					
					// TODO - match profile, navigate target class hierarchy
					
					ceList.add(cEntry);
				}					
			}
			
			Collections.sort(ceList);
						
			converter = new Converter<Object, Object, WebContext>() {
				
				@Override
				public Object convert(Object source, Class<Object> target, WebContext context) throws Exception {
					if (source == null) {
						return null;
					}
					if (target.isInstance(source)) {
						return source;
					}
					for (ConverterEntry ce: ceList) {
						if (ce.source.isInstance(source) && target.isAssignableFrom(ce.target)) {
							Object ret = ce.converter.convert(source, target, context);
							if (ret!=null) {
								return ret;
							}
						}
					}
					return null;
				}

				@Override
				public void close() throws Exception {
					for (ConverterEntry ce: ceList) {
						ce.converter.close();
					}
				}
			};
		}
		
		return converter;
	}
	
	private AuthorizationProvider authorizationProvider;
	
	public synchronized AuthorizationProvider getAuthorizationProvider() throws Exception {
		if (authorizationProvider == null) {
			class AuthorizationProviderEntry implements Comparable<AuthorizationProviderEntry> {
				
				public AuthorizationProviderEntry(AuthorizationProvider sm) {
					this.sm = sm;
				}
				
				int priority;
				
				AuthorizationProvider sm;

				@Override
				public int compareTo(AuthorizationProviderEntry o) {
					return o.priority - priority;
				}
				
			}
			final List<AuthorizationProviderEntry> smeList = new ArrayList<>();
			IConfigurationElement[] actionConfigurationElements = Platform.getExtensionRegistry().getConfigurationElementsFor(SECURITY_ID);
			for (IConfigurationElement ce: actionConfigurationElements) {
				if ("security_manager".equals(ce.getName())) {					
					AuthorizationProviderEntry sme = new AuthorizationProviderEntry((AuthorizationProvider) ce.createExecutableExtension("class"));
					
					String priorityStr = ce.getAttribute("priority");
					if (!isBlank(priorityStr)) {
						sme.priority = Integer.parseInt(priorityStr);
					}
					
					smeList.add(sme);
				}					
			}
			
			Collections.sort(smeList);
			
			authorizationProvider = new AuthorizationProvider() {
				
				@Override
				public Boolean authorize(Context context, Object target, String action) {
					for (AuthorizationProviderEntry sme: smeList) {
						Boolean result = sme.sm.authorize(context, target, action);
						if (result!=null) {
							return result;
						}
					}

					return Boolean.TRUE; // Allow by default.
				}
			};
		}
		return authorizationProvider;
	}
		
	public static boolean isBlank(String str) {
		return str==null || str.trim().length()==0;
	}
		
	public static String join(String[] sa, String separator) {
		StringBuilder sb = new StringBuilder();
		for (String pe: sa) {
			if (sb.length()>0) {
				sb.append(separator);
			}
			sb.append(pe);
		}
		return sb.toString();
	}

	public RouteRegistry getRouteRegistry() {
		return routeRegistry;
	}
	
	protected class MethodRoute extends InstanceMethodCommand<WebContext, Action> implements Route {
		
		protected MethodRoute(Object target, Method routeMethod) throws Exception {
			super(target, new MethodCommand<WebContext, Action>(routeMethod));
		}
		
	}
	
	private RouteRegistry routeRegistry = new RouteRegistry() {
		
		@Override
		public List<Route> matchObjectRoutes(RequestMethod method, Object target, String[] path) throws Exception {
			List<RouteEntry> collector = new ArrayList<RouteEntry>();
			List<RouteEntry> methodActions = getRoutes(RouteType.OBJECT, method);
			if (methodActions!=null) {
				for (RouteEntry ma: methodActions) {
					if (ma.match(target, path)) {
						collector.add(ma);
					}
				}
			}
			
			// Service routes			
			for (Entry<ServiceReference<Route>, Route> se: routeServiceTracker.getTracked().entrySet()) {	
				if ("object".equals(se.getKey().getProperty("type"))) {
					Object methodsProperty = se.getKey().getProperty("methods");					
					RequestMethod[] methods;
					if (methodsProperty==null || (methodsProperty instanceof String && "*".equals(((String) methodsProperty).trim()))) {
						methods = RequestMethod.values(); 
					} else if (methodsProperty instanceof String) {
						methods = new RequestMethod[] { RequestMethod.valueOf((String) methodsProperty) };
					} else if (methodsProperty instanceof String[]) {
						String[] msa = (String[]) methodsProperty; 
						methods = new RequestMethod[msa.length];
						for (int i=0; i<msa.length; ++i) {
							methods[i] = RequestMethod.valueOf(msa[i]);
						}
					} else {
						throw new IllegalArgumentException("Unexpected methods property type: "+methodsProperty);
					}
					Object priorityProperty = se.getKey().getProperty("priority");
					RouteEntry re = new RouteEntry(
							RouteDescriptor.RouteType.OBJECT, 
							methods, 
							(String) se.getKey().getProperty("pattern"), 
							bundleContext.getBundle().loadClass((String) se.getKey().getProperty("targetType")), 
							priorityProperty instanceof Integer ? ((Integer) priorityProperty).intValue() : 0, 
							se.getValue());
					if (re.match(target, path)) {
						collector.add(re);
					}
				}
			}
			
			if (target!=null) {
				for (final Method routeMethod: target.getClass().getMethods()) {
					ActionMethod amAnnotation = routeMethod.getAnnotation(ActionMethod.class);
					if (amAnnotation!=null) {
						RouteEntry re = new RouteEntry(RouteType.OBJECT, amAnnotation.value(), amAnnotation.pattern(), target.getClass(), amAnnotation.priority(), new MethodRoute(target, routeMethod)) {
							
							protected boolean match(Object obj, String[] path) {
								if (getPattern()==null && path.length>0 && !routeMethod.getName().equals(path[0])) {
									return false;
								}
								return super.match(obj, path);
							};
						};
						if (re.match(target, path)) {
							collector.add(re);
						}
					}
				}
			}

			Collections.sort(collector);
			List<Route> ret = new ArrayList<>();
			Z: for (RouteEntry re:collector) {
				for (RequestMethod rm: re.getMethods()) {
					if (rm.equals(method)) {
						ret.add(re.getRoute());
						continue Z;
					}
				}
			}
			return ret;
		}
		
		@Override
		public List<Route> matchRootRoutes(RequestMethod method, String[] path) throws Exception {
			List<RouteEntry> collector = new ArrayList<RouteEntry>();
			List<RouteEntry> methodActions = getRoutes(RouteDescriptor.RouteType.ROOT, method);
			if (methodActions!=null) {
				for (RouteEntry ma: methodActions) {
					if (ma.match(null, path)) {
						collector.add(ma);
					}
				}
			}
			
			// Service routes			
			for (Entry<ServiceReference<Route>, Route> se: routeServiceTracker.getTracked().entrySet()) {	
				if ("root".equals(se.getKey().getProperty("type"))) {
					Object methodsProperty = se.getKey().getProperty("methods");					
					RequestMethod[] methods;
					if (methodsProperty==null || (methodsProperty instanceof String && "*".equals(((String) methodsProperty).trim()))) {
						methods = RequestMethod.values(); 
					} else if (methodsProperty instanceof String) {
						methods = new RequestMethod[] { RequestMethod.valueOf((String) methodsProperty) };
					} else if (methodsProperty instanceof String[]) {
						String[] msa = (String[]) methodsProperty; 
						methods = new RequestMethod[msa.length];
						for (int i=0; i<msa.length; ++i) {
							methods[i] = RequestMethod.valueOf(msa[i]);
						}
					} else {
						throw new IllegalArgumentException("Unexpected methods property type: "+methodsProperty);
					}
					Object priorityProperty = se.getKey().getProperty("priority");
					RouteEntry re = new RouteEntry(
							RouteDescriptor.RouteType.ROOT, 
							methods, 
							(String) se.getKey().getProperty("pattern"), 
							null, 
							priorityProperty instanceof Integer ? ((Integer) priorityProperty).intValue() : 0, 
							se.getValue());
					if (re.match(null, path)) {
						collector.add(re);
					}
				}
			}
			
			Collections.sort(collector);
			List<Route> ret = new ArrayList<>();
			Z: for (RouteEntry re: collector) {
				for (RequestMethod rm: re.getMethods()) {
					if (rm.equals(method)) {
						ret.add(re.getRoute());
						continue Z;
					}
				}
			}
			return ret;
		}

		@Override
		public Route getExtensionRoute(RequestMethod method, Object target, String extension) throws Exception {
			List<RouteEntry> collector = new ArrayList<RouteEntry>();
			List<RouteEntry> methodActions = getRoutes(RouteDescriptor.RouteType.EXTENSION, method);
			if (methodActions!=null) {
				for (RouteEntry ma: methodActions) {
					if (ma.match(target, new String[] {extension})) {
						collector.add(ma);
					}
				}
			}
			
			// Service routes			
			for (Entry<ServiceReference<Route>, Route> se: routeServiceTracker.getTracked().entrySet()) {	
				if ("extension".equals(se.getKey().getProperty("type"))) {
					Object methodsProperty = se.getKey().getProperty("methods");					
					RequestMethod[] methods;
					if (methodsProperty==null || (methodsProperty instanceof String && "*".equals(((String) methodsProperty).trim()))) {
						methods = RequestMethod.values(); 
					} else if (methodsProperty instanceof String) {
						methods = new RequestMethod[] { RequestMethod.valueOf((String) methodsProperty) };
					} else if (methodsProperty instanceof String[]) {
						String[] msa = (String[]) methodsProperty; 
						methods = new RequestMethod[msa.length];
						for (int i=0; i<msa.length; ++i) {
							methods[i] = RequestMethod.valueOf(msa[i]);
						}
					} else {
						throw new IllegalArgumentException("Unexpected methods property type: "+methodsProperty);
					}
					Object priorityProperty = se.getKey().getProperty("priority");
					collector.add(new RouteEntry(
							RouteDescriptor.RouteType.OBJECT, 
							methods, 
							(String) se.getKey().getProperty("extension"), 
							bundleContext.getBundle().loadClass((String) se.getKey().getProperty("targetType")), 
							priorityProperty instanceof Integer ? ((Integer) priorityProperty).intValue() : 0, 
							se.getValue()));
				}
			}

			Collections.sort(collector);
			List<Route> ret = new ArrayList<>();
			Z: for (RouteEntry re:collector) {
				for (RequestMethod rm: re.getMethods()) {
					if (rm.equals(method)) {
						ret.add(re.getRoute());
						continue Z;
					}
				}
			}
			return ret.isEmpty() ? null : ret.get(0);
		}

	};
			
	protected class RouteEntry implements Comparable<RouteEntry> {
		
		private Pattern pattern;
		private RouteDescriptor.RouteType type;
		private Class<?> targetType;
		private int priority;
		private Route route;
		private RequestMethod[] methods;	

		public RouteEntry(
				RouteDescriptor.RouteType type,
				RequestMethod[] methods,
				String patternStr, 
				Class<?> targetType, 
				int priority, 
				Route route) {
			
			this.type = type;
			this.methods = methods;
			if (!isBlank(patternStr)) {
				pattern = Pattern.compile(patternStr);
			}
			this.targetType = targetType;
			this.priority = priority;
			this.route = route;
		}
		
		protected boolean match(Object obj, String[] path) {
			if (targetType!=null && !targetType.isInstance(obj)) {
				return false;
			}
			return pattern==null ? true : pattern.matcher(ExtensionManager.join(path, "/")).matches();			
		}

		protected Pattern getPattern() {
			return pattern;
		}

		public RouteDescriptor.RouteType getType() {
			return type;
		}
		
		public RequestMethod[] getMethods() {
			return methods;
		}

		protected Class<?> getTargetType() {
			return targetType;
		}

		protected int getPriority() {
			return priority;
		}

		protected Route getRoute() {
			return route;
		}

		@Override
		public int compareTo(RouteEntry o) {
			if (targetType==null) {
				if (o.targetType!=null) {
					return 1; // o is more specific.
				}
			} else {
				if (o.targetType==null) {
					return -1; // this entry is more specific.
				}
				
				if (targetType.isAssignableFrom(o.getTargetType())) {
					if (!o.getTargetType().isAssignableFrom(targetType)) {
						return 1; // o is more specific.
					}
				} else if (o.getTargetType().isAssignableFrom(targetType)) {
					return -1; // this entry is more specific.
				}
			}
			
			return o.getPriority()-getPriority();
		}
		
	}	
	
	private Map<RouteType, Map<RequestMethod, List<RouteEntry>>> routeMap;

	/**
	 * Registered actions
	 * @param method
	 * @return
	 * @throws Exception
	 */
	protected synchronized List<RouteEntry> getRoutes(RouteType routeType, RequestMethod method) throws Exception {
		if (routeMap == null) {
			routeMap = new HashMap<>();
			for (RouteType rt: RouteType.values()) {
				routeMap.put(rt, new HashMap<RequestMethod, List<RouteEntry>>());
			}

			IConfigurationElement[] actionConfigurationElements = Platform.getExtensionRegistry().getConfigurationElementsFor(ExtensionManager.ROUTE_ID);
			for (IConfigurationElement ce: actionConfigurationElements) {
				if ("object_route".equals(ce.getName())) {					
					Route route = (Route) ce.createExecutableExtension("class");		
					injectProperties(ce, route);
					String priorityStr = ce.getAttribute("priority");
					int priority = ExtensionManager.isBlank(priorityStr) ? 0 : Integer.parseInt(priorityStr);
					String targetClassName = ce.getAttribute("target");
					IContributor contributor = ce.getContributor();		
					Bundle bundle = Platform.getBundle(contributor.getName());
					Class<?> targetType = (Class<?>) bundle.loadClass(targetClassName.trim());
					String methodStr = ce.getAttribute("method");					
					RequestMethod[] routeMethods = "*".equals(methodStr) ? RequestMethod.values() : new RequestMethod[] {RequestMethod.valueOf(methodStr)};
					RouteEntry routeEntry = new RouteEntry(RouteType.OBJECT, routeMethods, ce.getAttribute("pattern"), targetType, priority, route);
										
					for (RequestMethod routeMethod: routeMethods) {
						List<RouteEntry> methodRoutes = routeMap.get(RouteType.OBJECT).get(routeMethod);
						if (methodRoutes == null) {
							methodRoutes = new ArrayList<>();
							routeMap.get(RouteType.OBJECT).put(routeMethod, methodRoutes);
						}
						methodRoutes.add(routeEntry);
					}
				} else if ("extension_route".equals(ce.getName())) {					
						Route route = (Route) ce.createExecutableExtension("class");		
						injectProperties(ce, route);
						String priorityStr = ce.getAttribute("priority");
						int priority = ExtensionManager.isBlank(priorityStr) ? 0 : Integer.parseInt(priorityStr);
						String targetClassName = ce.getAttribute("target");
						IContributor contributor = ce.getContributor();		
						Bundle bundle = Platform.getBundle(contributor.getName());
						Class<?> targetType = (Class<?>) bundle.loadClass(targetClassName.trim());
						String methodStr = ce.getAttribute("method");					
						RequestMethod[] routeMethods = "*".equals(methodStr) ? RequestMethod.values() : new RequestMethod[] {RequestMethod.valueOf(methodStr)};
						RouteEntry routeEntry = new RouteEntry(RouteType.EXTENSION, routeMethods, ce.getAttribute("extension"), targetType, priority, route);
											
						for (RequestMethod routeMethod: routeMethods) {
							List<RouteEntry> methodRoutes = routeMap.get(RouteType.EXTENSION).get(routeMethod);
							if (methodRoutes == null) {
								methodRoutes = new ArrayList<>();
								routeMap.get(RouteType.EXTENSION).put(routeMethod, methodRoutes);
							}
							methodRoutes.add(routeEntry);
						}
				} else if ("object_resource_route".equals(ce.getName())) {					
					String priorityStr = ce.getAttribute("priority");
					int priority = ExtensionManager.isBlank(priorityStr) ? 0 : Integer.parseInt(priorityStr);
					String targetClassName = ce.getAttribute("target");					
					IContributor contributor = ce.getContributor();		
					Bundle bundle = Platform.getBundle(contributor.getName());
					Class<?> targetType = (Class<?>) bundle.loadClass(targetClassName.trim());

					final String rName = ce.getAttribute("resource");			
					final URL baseURL = bundle.getResource(rName);
					
					final String contentType = ce.getAttribute("contentType");					
					
					Route route = new Route() {
						
						@Override
						public Action execute(final WebContext context) throws Exception {
							if (context.getPath().length==1) { // 0?
								return new Action() {
									
									@Override
									public Object execute() throws Exception {
										return baseURL;
									}

									@Override
									public void close() throws Exception {
										// NOP			
									}
								};
							}
							final String subPath = join(Arrays.copyOfRange(context.getPath(), 1, context.getPath().length), "/");
							return new Action() {
								
								@Override
								public Object execute() throws Exception {
									if (!isBlank(contentType) && context instanceof HttpContext) {
										HttpServletResponse resp = ((HttpContext) context).getResponse();
										if (isBlank(resp.getContentType())) {
											resp.setContentType(contentType);
										}
									}
									return new URL(baseURL, subPath);
								}

								@Override
								public void close() throws Exception {
									// NOP			
								}
								
							};
						}

						@Override
						public boolean canExecute() {
							return true;
						}

						@Override
						public void close() throws Exception {
							// NOP							
						}
						
					};
					
					RouteEntry routeEntry = new RouteEntry(RouteType.OBJECT, new RequestMethod[] {RequestMethod.GET}, ce.getAttribute("pattern"), targetType, priority, route);
					
					List<RouteEntry> methodRoutes = routeMap.get(RouteType.OBJECT).get(RequestMethod.GET);
					if (methodRoutes == null) {
						methodRoutes = new ArrayList<>();
						routeMap.get(RouteType.OBJECT).put(RequestMethod.GET, methodRoutes);
					}
					methodRoutes.add(routeEntry);
				} else if ("root_route".equals(ce.getName())) {					
					Route route = (Route) ce.createExecutableExtension("class");			
					injectProperties(ce, route);
					String priorityStr = ce.getAttribute("priority");
					int priority = ExtensionManager.isBlank(priorityStr) ? 0 : Integer.parseInt(priorityStr);
					String methodStr = ce.getAttribute("method");					
					RequestMethod[] routeMethods = "*".equals(methodStr) ? RequestMethod.values() : new RequestMethod[] {RequestMethod.valueOf(methodStr)};
					RouteEntry routeEntry = new RouteEntry(RouteType.ROOT, routeMethods, ce.getAttribute("pattern"), null, priority, route);
					
					for (RequestMethod routeMethod: routeMethods) {
						List<RouteEntry> methodRoutes = routeMap.get(RouteType.ROOT).get(routeMethod);
						if (methodRoutes == null) {
							methodRoutes = new ArrayList<>();
							routeMap.get(RouteType.ROOT).put(routeMethod, methodRoutes);
						}
						methodRoutes.add(routeEntry);
					}
				} else if ("root_resource_route".equals(ce.getName())) {					
					String priorityStr = ce.getAttribute("priority");
					int priority = ExtensionManager.isBlank(priorityStr) ? 0 : Integer.parseInt(priorityStr);
					IContributor contributor = ce.getContributor();		
					Bundle bundle = Platform.getBundle(contributor.getName());

					final String rName = ce.getAttribute("resource");			
					final URL baseURL = bundle.getResource(rName);
					
					final String contentType = ce.getAttribute("contentType");					
					
					final Route route = new Route() {
						
						@Override
						public Action execute(final WebContext context) throws Exception {
							if (context.getPath().length==1) { // 0?
								return new Action() {
									
									@Override
									public Object execute() throws Exception {
										return baseURL;
									}

									@Override
									public void close() throws Exception {
										// NOP			
									}
								};
							}
							final String subPath = join(Arrays.copyOfRange(context.getPath(), 1, context.getPath().length), "/");
							return new Action() {
								
								@Override
								public Object execute() throws Exception {
									if (!isBlank(contentType) && context instanceof HttpContext) {
										HttpServletResponse resp = ((HttpContext) context).getResponse();
										if (isBlank(resp.getContentType())) {
											resp.setContentType(contentType);
										}
									}
									return new URL(baseURL, subPath);
								}

								@Override
								public void close() throws Exception {
									// NOP			
								}
								
							};
						}

						@Override
						public boolean canExecute() {
							return true;
						}

						@Override
						public void close() throws Exception {
							// NOP							
						}
						
					};
					
					RouteEntry routeEntry = new RouteEntry(RouteType.ROOT, new RequestMethod[] { RequestMethod.GET } , ce.getAttribute("pattern"), null, priority, route);
					
					List<RouteEntry> methodRoutes = routeMap.get(RouteType.ROOT).get(RequestMethod.GET);
					if (methodRoutes == null) {
						methodRoutes = new ArrayList<>();
						routeMap.get(RouteType.ROOT).put(RequestMethod.GET, methodRoutes);
					}
					methodRoutes.add(routeEntry);
				} else if ("route_provider".equals(ce.getName())) {
					RouteProvider routeProvider = (RouteProvider) ce.createExecutableExtension("class");
					injectProperties(ce, routeProvider);
					for (final RouteDescriptor routeDescriptor: routeProvider.getRouteDescriptors()) {
						RouteEntry routeEntry = new RouteEntry(routeDescriptor.getType(), routeDescriptor.getMethods(), routeDescriptor.getPattern(), routeDescriptor.getTarget(), routeDescriptor.getPriority(), routeDescriptor.getRoute());
																
						for (RequestMethod routeMethod: routeDescriptor.getMethods()) {
							List<RouteEntry> methodRoutes = routeMap.get(routeDescriptor.getType()).get(routeMethod);
							if (methodRoutes == null) {
								methodRoutes = new ArrayList<>();
								routeMap.get(routeDescriptor.getType()).put(routeMethod, methodRoutes);
							}
							methodRoutes.add(routeEntry);
						}
						
					}
					
				}
			}

			for (Map<RequestMethod, List<RouteEntry>> rm: routeMap.values()) {
				for (List<RouteEntry> ame: rm.values()) {
					Collections.sort(ame);
				}
			}
		}
		
		List<RouteEntry> ret = routeMap.get(routeType).get(method);
		return ret == null ? Collections.<RouteEntry>emptyList() : ret;
	}

	public static void injectProperties(IConfigurationElement ce, final Object target) throws IllegalAccessException, InvocationTargetException {
		for (IConfigurationElement cce: ce.getChildren()) {
			if ("property".equals(cce.getName())) {
				injectProperty(target, cce.getAttribute("name").split("\\."), cce.getAttribute("value"));
			}
		}
	}

	private static void injectProperty(Object target, String[] propertyPath, String value) throws IllegalAccessException, IllegalArgumentException, InvocationTargetException {
		if (propertyPath.length==1) {
			String mName = "set"+propertyPath[0].substring(0, 1).toUpperCase()+propertyPath[0].substring(1);
			for (Method mth: target.getClass().getMethods()) {
				Class<?>[] pTypes = mth.getParameterTypes();
				if (pTypes.length==1 && mth.getName().equals(mName) && pTypes[0].isAssignableFrom(String.class)) {
					mth.invoke(target, value);
					return;
				}
			}
			throw new IllegalArgumentException("Method "+mName+"(String) not found in "+target.getClass().getName());
		} else if (propertyPath.length>1) {
			String mName = "get"+propertyPath[0].substring(0, 1).toUpperCase()+propertyPath[0].substring(1);
			for (Method mth: target.getClass().getMethods()) {
				if (mth.getParameterTypes().length==0 && mth.getName().equals(mName)) {
					Object nextTarget = mth.invoke(target);
					if (nextTarget == null) {
						throw new NullPointerException("Cannot set property: "+mth+" returned null");
					}
					injectProperty(nextTarget, Arrays.copyOfRange(propertyPath, 1, propertyPath.length), value);
					return;
				}
			}
			throw new IllegalArgumentException("Method "+mName+"(String) not found in "+target.getClass().getName());			
		}
	}

	@Override
	public void close() throws Exception {
		routeServiceTracker.close();
		
		if (converter!=null) {
			converter.close();
		}
		
		// Closing routes.
		if (routeMap!=null) {
			for (Map<RequestMethod, List<RouteEntry>> rm: routeMap.values()) {
				for (List<RouteEntry> rl: rm.values()) {
					for (RouteEntry r: rl) {
						if (r instanceof AutoCloseable) {
							try {
								((AutoCloseable) r).close();
							} catch (Exception e) {
								e.printStackTrace();
							}
						}
					}
				}
			}
		}
	}

	public HTMLFactory getHTMLFactory() {
		return htmlFactory;
	}	
	
}
