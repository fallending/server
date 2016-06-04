package org.nasdanika.web;

import java.lang.annotation.Annotation;
import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.regex.Pattern;

import org.apache.commons.lang3.StringUtils;
import org.eclipse.jetty.websocket.api.Session;
import org.eclipse.jetty.websocket.api.WebSocketListener;
import org.nasdanika.core.Context;
import org.nasdanika.core.ContextParameter;
import org.nasdanika.core.ContextProvider;
import org.nasdanika.core.CoreUtil;
import org.osgi.framework.BundleContext;

/**
 * Web method command for methods annotated with {@link RouteMethod}.
 * @author Pavel Vlasov
 *
 * @param <C>
 * @param <R>
 */
public class RouteMethodCommand<C extends HttpServletRequestContext, R> extends WebMethodCommand<C, R> implements Comparable<RouteMethodCommand<C, R>> {
	
	private String path;
	private RequestMethod[] requestMethods;
	private Pattern pattern;
	private String produces;
	private String[] consumes;
	private String action;
	private String qualifier;
	private String comment;
	
	public String getComment() {
		return comment;
	}
	
	public String getPath() {
		return path;
	}
	
	public RequestMethod[] getRequestMethods() {
		return requestMethods;
	}
	
	public Pattern getPattern() {
		return pattern;
	}

	public RouteMethodCommand(BundleContext bundleContext, Method method) throws Exception {
		super(bundleContext, method);
		RouteMethod routeMethod = method.getAnnotation(RouteMethod.class);
		if (routeMethod == null) {
			throw new IllegalArgumentException("Not annotated with @RouteMethod: "+method);
		}
				
		path = routeMethod.path();						
		requestMethods = routeMethod.value();
		comment = routeMethod.comment();
		
		// Implying path and possibly request method from method name
		if (CoreUtil.isBlank(path) && CoreUtil.isBlank(routeMethod.pattern())) {
			boolean requestMethodMatched = false;
			if (requestMethods.length==0) {
				String[] smn = StringUtils.splitByCharacterTypeCamelCase(method.getName());
				RM: for (RequestMethod rm: RequestMethod.values()) {
					String[] rmna = rm.name().split("_");
					if (smn.length>rmna.length) {
						for (int idx = 0; idx < rmna.length; ++idx) {
							if (!smn[idx].equalsIgnoreCase(rmna[idx])) {
								continue RM;
							}
						}
						requestMethods = new RequestMethod[] {rm};
						StringBuilder pathBuilder = new StringBuilder();
						for (int i = rmna.length; i < smn.length; ++i) {
							if (i>rmna.length) {
								pathBuilder.append(RequestMethod.GET == rm && i == smn.length-1 ? "." : "/");
							}
							pathBuilder.append(smn[i].toLowerCase());
						}
						path = pathBuilder.toString();
						requestMethodMatched = true;
						break;
					}
				}
			}
			if (!requestMethodMatched) {
				path = method.getName();
			}
		}
		
		if (CoreUtil.isBlank(path) && !CoreUtil.isBlank(routeMethod.pattern())) {
			pattern = Pattern.compile(routeMethod.pattern());
		}
		
		produces = routeMethod.produces();
		if (CoreUtil.isBlank(produces)) {
			String shortPath = path.substring(path.lastIndexOf("/")+1);
			String impliedProduces = AbstractRoutingServlet.MIME_TYPES_MAP.getContentType(shortPath);
			if (!CoreUtil.isBlank(impliedProduces)) {
				produces = impliedProduces;
			}
		}
		consumes = routeMethod.consumes();
		action = routeMethod.action();
		qualifier = routeMethod.qualifier();
		
	}
	
	public String getProduces() {
		return produces;
	}
	
	public String[] getConsumes() {
		return consumes;
	}
	
	public String getAction() {
		return action;
	}
	
	public String getQualifier() {
		return qualifier;
	}
	
	@Override
	protected ArgumentResolver<C> createArgumentResolver(final Class<?> parameterType, Annotation[] parameterAnnotations) throws Exception {
		
		// A trick to shift context passed as a method argument and still be able to get access to path parameters.
		for (Annotation a: parameterAnnotations) {
			// Context parameter
			if (ContextParameter.class.isInstance(a)) {
				return new ArgumentResolver<C>() {
					
					@Override
					public Object getValue(HttpServletRequestContext context, Object[] args, Annotation[] parameterAnnotations) throws Exception {
						if (!CoreUtil.isBlank(path)) {
							context = context.shift(path.split("/").length);
						}
						if (parameterType.isInstance(context)) {
							return context;
						}
						
						return context.adapt(parameterType);
					}
					
					@Override
					public void close() {
						// NOP						
					}
				};
			}
		}						
		return super.createArgumentResolver(parameterType, parameterAnnotations);
	}
		
	@Override
	public int compareTo(RouteMethodCommand<C, R> o) {
		RouteMethod rm1 = getMethod().getAnnotation(RouteMethod.class);
		RouteMethod rm2 = o.getMethod().getAnnotation(RouteMethod.class);
		
		// The route with higher priority takes precedence.
		int cmp = rm2.priority() - rm1.priority();
		if (cmp!=0) {
			return cmp;
		}
		
		// If priorities are equal, then route defined in a sub-class takes precedence.
		Class<?> dc1 = getMethod().getDeclaringClass();
		Class<?> dc2 = o.getMethod().getDeclaringClass();
		if (dc1!=dc2) {
			return dc1.isAssignableFrom(dc2) ? 1 : -1;
		}
		
		// If the routes have the same inheritance distance then a route with path takes precedence over a route with pattern.
		if (!CoreUtil.isBlank(rm1.path())) {
			if (CoreUtil.isBlank(rm2.path())) {
				return -1; 
			}
		}
		
		if (!CoreUtil.isBlank(rm2.path())) {
			if (CoreUtil.isBlank(rm1.path())) {
				return 1; 
			}
		}
		
		// A route with the longest path/pattern takes precedence over the other if both use path or pattern.				
		String p1 = CoreUtil.isBlank(getPath()) ? rm1.pattern() : getPath();
		String p2 = CoreUtil.isBlank(o.getPath()) ? rm2.pattern() : o.getPath();
		cmp = p2.length() - p1.length();
		if (cmp!=0) {
			return cmp;
		}
		
		return hashCode() - o.hashCode();
	}
	
	/**
	 * Wrapping ContextWebSocketListener
	 */
	@SuppressWarnings("unchecked")
	@Override
	public R execute(C context, Object target, Object[] arguments) throws Exception {
		R ret = super.execute(context, target, arguments);
		if (context.getMethod() == RequestMethod.CREATE_WEB_SOCKET) {
			WebSocketUpgradeInfo webSocketUpgradeInfo = context.adapt(WebSocketUpgradeInfo.class);
			if (webSocketUpgradeInfo == null) {
				throw new NullPointerException("WebSocketUpgradeInfo is null");
			}
			ContextProvider<?> contextProvider = webSocketUpgradeInfo.getContextProvider(context);
			
			if (ret instanceof ContextWebSocketListener) {				
				return (R) new WebSocketListener() {
					
					@Override
					public void onWebSocketError(Throwable cause) {
						try (Context ctx = contextProvider.createContext()) {
							((ContextWebSocketListener<Context>) ret).onWebSocketError(ctx, cause);
						} catch (Exception e) {
							e.printStackTrace();
						}
					}
					
					@Override
					public void onWebSocketConnect(Session session) {
						try (Context ctx = contextProvider.createContext()) {
							((ContextWebSocketListener<Context>) ret).onWebSocketConnect(ctx, session);
						} catch (Exception e) {
							e.printStackTrace();
						}
					}
					
					@Override
					public void onWebSocketClose(int statusCode, String reason) {
						try (Context ctx = contextProvider.createContext()) {
							((ContextWebSocketListener<Context>) ret).onWebSocketClose(ctx, statusCode, reason);
						} catch (Exception e) {
							e.printStackTrace();
						}
					}
					
					@Override
					public void onWebSocketText(String message) {
						try (Context ctx = contextProvider.createContext()) {
							((ContextWebSocketListener<Context>) ret).onWebSocketText(ctx, message);
						} catch (Exception e) {
							e.printStackTrace();
						}
					}
					
					@Override
					public void onWebSocketBinary(byte[] payload, int offset, int len) {
						try (Context ctx = contextProvider.createContext()) {
							((ContextWebSocketListener<Context>) ret).onWebSocketBinary(ctx, payload, offset, len);
						} catch (Exception e) {
							e.printStackTrace();
						}
					}
				};
			} 
			throw new IllegalArgumentException("Return value must be an instance of "+ContextWebSocketListener.class.getName());
		}
		return ret;
	}

	@Override
	public String toString() {
		return "RouteMethodCommand [path=" + path + ", requestMethods=" + Arrays.toString(requestMethods) + ", pattern="
				+ pattern + ", produces=" + produces + ", consumes=" + Arrays.toString(consumes) + ", action=" + action
				+ ", qualifier=" + qualifier + ", comment=" + comment + ", method=" + method + "]";
	}
		
}
