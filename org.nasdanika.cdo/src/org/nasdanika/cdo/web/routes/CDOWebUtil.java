package org.nasdanika.cdo.web.routes;

import java.lang.reflect.Array;
import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Map.Entry;

import javax.servlet.http.HttpServletResponse;

import org.eclipse.emf.cdo.CDOObject;
import org.eclipse.emf.cdo.common.id.CDOID;
import org.eclipse.emf.cdo.common.id.CDOIDUtil;
import org.eclipse.emf.cdo.common.revision.CDORevision;
import org.eclipse.emf.cdo.view.CDOView;
import org.eclipse.emf.common.util.BasicEList;
import org.eclipse.emf.common.util.EList;
import org.eclipse.emf.ecore.EAttribute;
import org.eclipse.emf.ecore.EClass;
import org.eclipse.emf.ecore.EObject;
import org.eclipse.emf.ecore.EOperation;
import org.eclipse.emf.ecore.EPackage;
import org.eclipse.emf.ecore.EReference;
import org.eclipse.emf.ecore.EStructuralFeature;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.nasdanika.cdo.CDOViewContext;
import org.nasdanika.core.ConverterContext;
import org.nasdanika.core.JSONLoader;
import org.nasdanika.web.ServerException;
import org.nasdanika.web.WebContext;

public class CDOWebUtil {
	
	public static final String TYPE_KEY = "$type";

	public static final String PATH_KEY = "$path";

	public static final String VERSION_KEY = "$version";

	public static final String VALUE_KEY = "value";

	public static final String INITIAL_VALUE_KEY = "initialValue";
	
	/**
	 * Operation annotation instructs to render operation as a property getter. Shall be used for operations which take
	 * a single argument through which invocation context is passed.
	 * If operation name starts with <code>get</code> then the property name is computed by removing the <code>get</code>
	 * prefix and uncapitalizing the rest of the operation name, otherwise the property name equals the operation name.
	 */
	public static final String ANNOTATION_GETTER = "org.nasdanika.cdo.web:getter";
	
	/**
	 * Operation annotation instructs to render operation as a property setter. Shall be used for operations which take
	 * two arguments - context and property value.
	 * If operation name starts with <code>set</code> then the property name is computed by removing the <code>set</code>
	 * prefix and uncapitalizing the rest of the operation name, otherwise the property name equals the operation name.
	 */
	public static final String ANNOTATION_SETTER = "org.nasdanika.cdo.web:setter";
	
	/**
	 * Strict optimistic locking - for objects initial version shall match the version in the repository, for collections all initial
	 * elements shall be the same. Default for many-references.
	 */
	public static final String ANNOTATION_STRICT = "org.nasdanika.cdo.web:strict";
	
	/**
	 * Lenient optimistic locking - for objects only initial values shall match, for references only changes are submitted to the server 
	 * side, e.g. additions. Default for objects.
	 */
	public static final String ANNOTATION_LENIENT = "org.nasdanika.cdo.web:lenient";
	
	/**
	 * Details of this annotation are generated as facade declarations with keys as object keys and values are values.
	 */
	public static final String ANNOTATION_FACADE_ENTRIES = "org.nasdanika.cdo.web:facade-entries";
	
	/**
	 * Marks a structural feature as server-side only, i.e. suppresses generation of JavaScript code for it.
	 */
	public static final String ANNOTATION_PRIVATE = "org.nasdanika.cdo.web:private";
	
	/**
	 * Instructs to load references and objects eagerly before this object. It results in
	 * fewer larger requests and simple synchronous reference properties - an array of objects.
	 */
	public static final String ANNOTATION_EAGER_OBJ = "org.nasdanika.cdo.web:eager-obj";
	
	/**
	 * Eagerly loads a list of object id's for a reference. Objects themselves are pre-loaded asynchronously.
	 * Reference property is an array of promises for objects. 
	 */
	public static final String ANNOTATION_EAGER_REF = "org.nasdanika.cdo.web:eager-ref"; 
	
	/**
	 * Lazily loads a list of object ID's. Objects are pre-loaded when reference is retrieved. 
	 * Reference property is a promise for an array of promises for objects.
	 * This is a default strategy for many references.
	 */
	public static final String ANNOTATION_LAZY_REF = "org.nasdanika.cdo.web:lazy-ref"; // Default for many
			
	/**
	 * Lazily loads a list of object ID's. Objects are loaded when reference is retrieved. 
	 * Reference property is a promise for an array of objects.
	 * This is a default strategy for one references.
	 */		
	public static final String ANNOTATION_LAZY_OBJ = "org.nasdanika.cdo.web:lazy-obj"; // Default for one
	
	/**
	 * Loads a list of object ID's and referenced objects asynchronously when the object is loaded. 
	 * Reference property is a promise for an array of promises for objects.
	 */				
	public static final String ANNOTATION_PRELOAD_REF = "org.nasdanika.cdo.web:preload-ref";
	
	/**
	 * Loads a list of object ID's asynchronously, referenced objects are loaded eagerly once the reference is loaded. 
	 * Reference property is a promise for an array of objects.
	 */				
	public static final String ANNOTATION_PRELOAD_OBJ = "org.nasdanika.cdo.web:preload-obj";
	

	private CDOWebUtil() {
		// Utility class
	}
		
	public static CDOObject resolvePath(WebContext context,  String path) throws Exception {		
		CDOView view = context.adapt(CDOViewContext.class).getView();
		String viewObjectsPath = context.getObjectPath(view)+"/objects/";
		if (!path.startsWith(viewObjectsPath)) {
			throw new ServerException("Foreign object: "+path, HttpServletResponse.SC_NOT_FOUND);
		}
		String objPath = path.substring(viewObjectsPath.length());
		CDOID cdoID = CDOIDUtil.read(objPath);
		return view.getObject(cdoID);
	}
	
	/**
	 * Retrieves object value converted to requested type.
	 * @param obj
	 * @param key
	 * @param type
	 * @return
	 */
	public static Object get(ConverterContext context, JSONObject json, String key, Class<?> type) throws Exception {
		if (json.has(key)) {
			switch (type.getName()) {
			case "boolean":
			case "java.lang.Boolean":
				return json.getBoolean(key);
			case "double":
			case "java.lang.Double":
				return json.getDouble(key);
			case "int":
			case "java.lang.Integer":
				return json.getInt(key);
			case "long":
			case "java.lang.Long":
				return json.getLong(key);
			case "java.lang.String":
				return json.getString(key);
			case "java.lang.Object":
				return json.get(key);
			default:
				return context.convert(json.get(key), type);				
			}
		}			
		return null;
	}

	/**
	 * Retrieves object value converted to requested type.
	 * @param obj
	 * @param key
	 * @param type
	 * @return
	 */
	public static Object get(ConverterContext context, JSONArray json, int idx, Class<?> type) throws Exception {
		switch (type.getName()) {
		case "boolean":
		case "java.lang.Boolean":
			return json.getBoolean(idx);
		case "double":
		case "java.lang.Double":
			return json.getDouble(idx);
		case "int":
		case "java.lang.Integer":
			return json.getInt(idx);
		case "long":
		case "java.lang.Long":
			return json.getLong(idx);
		case "java.lang.String":
			return json.getString(idx);
		case "java.lang.Object":
			return json.get(idx);
		default:
			return context.convert(json.get(idx), type);				
		}
	}
	
	public static Object[] get(ConverterContext context, JSONArray json, Class<?> type) throws Exception {
		Object[] ret = new Object[json.length()];
		for (int i=0; i<json.length(); ++i) {
			ret[i] = get(context, json, i, type);
		}
		return ret;
	}
	
	public static CDOObject resolveOrCreate(WebContext context, JSONObject json, EClass baseType) throws Exception {
		if (json.has(PATH_KEY)) {
			return resolvePath(context, json.getString(PATH_KEY));
		}
		if (json.has(VALUE_KEY)) {
			return create(context, json.getJSONObject(VALUE_KEY), baseType);
		}
		throw new ServerException("Invalid input: "+json, HttpServletResponse.SC_BAD_REQUEST);
	}
	
	/**
	 * Resolves if <code>$path</code> is set, creates if <code>value</code> is set.
	 * @param json
	 * @return
	 * @throws Exception 
	 */
	public static CDOObject create(WebContext context, JSONObject json, EClass baseType) throws Exception {
		EClass retType = baseType;
		if (json.has(TYPE_KEY)) {
			String typeName = json.getString(TYPE_KEY);
			int idx = typeName.indexOf('@');
			if (idx==-1) {
				if (baseType==null) {
					throw new ServerException("Invalid type: "+typeName, HttpServletResponse.SC_BAD_REQUEST);
				} 
				retType = (EClass) baseType.getEPackage().getEClassifier(typeName);
			} else {
				EPackage pkg = context.adapt(CDOViewContext.class).getView().getSession().getPackageRegistry().getEPackage(typeName.substring(idx+1));
				if (pkg==null) {
					throw new ServerException("Invalid type: "+typeName, HttpServletResponse.SC_BAD_REQUEST);
				}
				retType = (EClass) pkg.getEClassifier(typeName.substring(0, idx));
			}
			if (retType==null) {
				throw new ServerException("Invalid type: "+typeName, HttpServletResponse.SC_BAD_REQUEST);
			}
		}
		if (retType==null) {
			throw new ServerException("Untyped object", HttpServletResponse.SC_BAD_REQUEST);
		}
		if (retType.isAbstract()) {
			throw new ServerException("Abstract type: "+retType.getName(), HttpServletResponse.SC_BAD_REQUEST);
		}
		EObject ret = retType.getEPackage().getEFactoryInstance().create(retType);
		if (ret instanceof JSONLoader) {
			((JSONLoader) ret).loadJSON(json, context);
		} else {
			@SuppressWarnings("unchecked")
			Iterator<String> kit = json.keys();
			while (kit.hasNext()) {
				String key = kit.next();
				if (!TYPE_KEY.equals(key)) {
					EStructuralFeature feature = retType.getEStructuralFeature(key);
					if (feature==null) {
						throw new ServerException("Invalid feature: "+key+" in "+retType.getName(), HttpServletResponse.SC_NOT_FOUND);							
					}
					if (!feature.isChangeable() || feature.getEAnnotation(ANNOTATION_PRIVATE)!=null) {
						throw new ServerException(HttpServletResponse.SC_NOT_FOUND);
					}
					// TODO - check authorization somehow?
	//				if (!context.authorize(???, "write", feature.getName(), null)) {
	//					throw new ServerException(HttpServletResponse.SC_FORBIDDEN);
	//				}
					if (feature.isMany()) {
						@SuppressWarnings("unchecked")
						Collection<Object> val = (Collection<Object>) ret.eGet(feature);
						JSONArray jva = json.getJSONArray(key);
						for (int i=0; i<jva.length(); ++i) {
							val.add(feature instanceof EAttribute ? get(context, json, key, feature.getEType().getInstanceClass()) : resolveOrCreate(context, json.getJSONObject(key), (EClass) feature.getEType()));
						}
					} else {
						ret.eSet(feature, feature instanceof EAttribute ? get(context, json, key, feature.getEType().getInstanceClass()) : resolveOrCreate(context, json.getJSONObject(key), (EClass) feature.getEType()));
					}
				}
			}
		}		
		return (CDOObject) ret;
	}
	
	/**
	 * Used to filter out definitions which did not change in the deltas scenario.
	 * @author Pavel
	 *
	 */
	public static interface DataDefinitionFilter {
		
		boolean accept(WebContext context, CDOObject cdoObject, EStructuralFeature feature, JSONObject definition) throws Exception;
		
	}
	
	public static JSONObject generateDataDefinitions(WebContext context, CDOObject cdoObject, DataDefinitionFilter filter) throws Exception {
		// someAttr: { initialValue: 33 }       
		JSONObject ret = new JSONObject(); 
		EClass eClass = cdoObject.eClass();
		if (eClass.getEAnnotation(CDOWebUtil.ANNOTATION_PRIVATE)==null) {
			CDORevision rev = cdoObject.cdoRevision();
			if (rev!=null) {
				ret.put(VERSION_KEY, rev.getVersion());
			}
			if (context.authorize(cdoObject, "read", null, null)) {
				EObject container = cdoObject.eContainer();
				if (container!=null) {
					JSONObject cv = new JSONObject();
					cv.put(INITIAL_VALUE_KEY, context.getObjectPath(container));
					ret.put("$container", cv);
				}
			}
			for (EAttribute attr: eClass.getEAllAttributes()) {
				if (attr.getEAnnotation(CDOWebUtil.ANNOTATION_PRIVATE)==null) {
					generateDataDefinition(context, cdoObject, ret, attr, filter);
				}
			}
			for (EReference ref: eClass.getEAllReferences()) {
				if (ref.getEAnnotation(CDOWebUtil.ANNOTATION_PRIVATE)==null) {
					generateDataDefinition(context, cdoObject, ret, ref, filter);
				}
			}
		}
		return ret;
	}

	private static void generateDataDefinition(
			WebContext context, 
			CDOObject cdoObject, 
			JSONObject dataDefinitions,
			EAttribute attr, 
			DataDefinitionFilter filter) throws Exception {
		
		if (context.authorize(cdoObject, "read", attr.getName(), null)) {
			JSONObject dd = new JSONObject();
			if (cdoObject.eIsSet(attr)) {
				if (attr.isMany()) {
					JSONArray da = new JSONArray();
					dd.put("initialValue", da);
					for (Object e: (Collection<?>) cdoObject.eGet(attr)) {
						da.put(e);
					}
				} else {
					dd.put("initialValue", cdoObject.eGet(attr));
				}
			}
			if (filter==null || filter.accept(context, cdoObject, attr, dd)) {
				dataDefinitions.put(attr.getName(), dd);
			}
		} else if (context.authorize(cdoObject, "write", attr.getName(), null)) {
			dataDefinitions.put(attr.getName(), new JSONObject());
		}
	}

	private static void generateDataDefinition(
			WebContext context, 
			CDOObject cdoObject, 
			JSONObject dataDefinitions,
			EReference ref, 
			DataDefinitionFilter filter) throws Exception {
		
		if (context.authorize(cdoObject, "read", ref.getName(), null)) {
			
			JSONObject dd = new JSONObject();
			if ((ref.getEAnnotation(CDOWebUtil.ANNOTATION_EAGER_OBJ)!=null || ref.getEAnnotation(CDOWebUtil.ANNOTATION_EAGER_REF)!=null || !ref.isMany())
					&& ref.getEAnnotation(CDOWebUtil.ANNOTATION_LAZY_OBJ)==null
					&& ref.getEAnnotation(CDOWebUtil.ANNOTATION_LAZY_REF)==null) {
				Object value = cdoObject.eGet(ref);
				if (value!=null) {
					if (ref.isMany()) {
						JSONArray da = new JSONArray();
						dd.put("initialValue", da);
						for (Object e: (Collection<?>) value) {
							da.put(context.getObjectPath(e));
						}
					} else {
						dd.put("initialValue", context.getObjectPath(value));
					}
				}
			}
			if (filter==null || filter.accept(context, cdoObject, ref, dd)) {
				dataDefinitions.put(ref.getName(), dd);
			}
		} 
	}

	public static EList<?> unmarshal(WebContext context, JSONArray input, Class<?>[] parameterTypes, EClass contextType) throws Exception {
		EList<Object> ret = new BasicEList<>();
		for (int i=0; i<input.length(); ++i) {
			Object el = input.get(i);
			if (el instanceof JSONArray) {
				ret.add(unmarshal(context, (JSONArray) el, contextType));
			} else if (el instanceof JSONObject) {
				ret.add(unmarshal(context, (JSONObject) el, parameterTypes[i], contextType));
			} else {
				ret.add(get(context,input, i, parameterTypes[i]));
			}
		}
		return ret;		
	}
	
	public static Object[] unmarshal(WebContext context, JSONArray input, EClass contextType) throws Exception {
		Object[] ret = new Object[input.length()];
		for (int i=0; i<input.length(); ++i) {
			Object el = input.get(i);
			if (el instanceof JSONArray) {
				ret[i] = unmarshal(context, (JSONArray) el, contextType);
			} else if (el instanceof JSONObject) {
				ret[i] = unmarshal(context, (JSONObject) el, null, contextType);
			} else {
				ret[i] = el;
			}
		}
		return ret;
	}

	public static Object unmarshal(WebContext context, JSONObject json, Class<?> valueType, EClass contextType) throws Exception {
		// $path -> resolve, value -> create or map
		if (json.has(PATH_KEY)) {
			return resolvePath(context, json.getString(PATH_KEY));
		}
		if (json.has(VALUE_KEY)) {
			if (json.has(TYPE_KEY)) {
				return create(context, json.getJSONObject(VALUE_KEY), contextType);
			}
			if (valueType!=null) {
				return get(context, json, VALUE_KEY, valueType);				
			}
			
			Object val = json.get(VALUE_KEY);
			if (val instanceof JSONObject) {			
				Map<String, Object> ret = new HashMap<>();
				JSONObject jsonVal = (JSONObject) val;
				@SuppressWarnings("unchecked")
				Iterator<String> kit = jsonVal.keys();
				while (kit.hasNext()) {
					String key = kit.next();
					Object e = jsonVal.get(key);
					if (e instanceof JSONArray) {
						ret.put(key, unmarshal(context, (JSONArray) e, contextType));
					} else if (e instanceof JSONObject) {
						ret.put(key, unmarshal(context, (JSONObject) e, null, contextType));
					} else {
						ret.put(key, e);
					}				
				}
				return ret;
			}
			return val;
		}
		throw new ServerException("Invalid input: "+json, HttpServletResponse.SC_BAD_REQUEST);
	}

	@SuppressWarnings("unchecked")
	public static Object marshal(WebContext context, Object result) throws Exception {
		if (result==null) {
			return null;
		}
		if (result.getClass().isArray()) {
			JSONArray ret = new JSONArray();
			for (int i=0, l=Array.getLength(result); i<l; ++i) {
				ret.put(Array.get(ret, i));
			}
			return ret;
		}
		if (result instanceof Collection) {
			JSONArray ret = new JSONArray();
			for (Object el: (Collection<?>) result) {
				ret.put(el);
			}
			return ret;			
		}
		if (result instanceof Map) {
			JSONObject ret = new JSONObject();
			for (Entry<String, Object> e: ((Map<String,Object>) result).entrySet()) {
				ret.put(e.getKey(), marshal(context, e.getValue()));
			}
			return marshalValue(ret);
		}
		JSONObject ret = new JSONObject();
		if (result instanceof CDOObject) {
			ret.put(PATH_KEY, context.getObjectPath(result));
		}
		// Value
		ret.put(VALUE_KEY, result);
		return ret;
	}
	
	public static JSONObject marshalValue(Object val) throws JSONException {
		JSONObject ret = new JSONObject();
		ret.put(VALUE_KEY, val);
		return ret;
		
	}
	
	public static JSONObject marshalReference(WebContext context, CDOObject target) throws Exception {
		JSONObject ret = new JSONObject();
		ret.put(PATH_KEY, context.getObjectPath(target));
		return ret;
	}
	
	/**
	 * read for getters, write for setters, invoke otherwise
	 * @param op
	 * @return
	 */
	public static String getEOperationPermission(EOperation op) {
		if (op.getEAnnotation(ANNOTATION_GETTER)!=null) {
			return "read";
		}
		if (op.getEAnnotation(ANNOTATION_SETTER)!=null) {
			return "write";
		}
		return "invoke";
	}

}