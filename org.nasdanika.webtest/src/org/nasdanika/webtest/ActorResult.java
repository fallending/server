package org.nasdanika.webtest;

import java.io.OutputStreamWriter;
import java.io.Writer;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import org.json.JSONArray;
import org.json.JSONObject;
import org.openqa.selenium.WebDriver;

/**
 * Contains results of actor use throughout tests.
 * @author Pavel Vlasov
 *
 */
public class ActorResult implements HttpPublisher {

	private final Class<? extends Actor<WebDriver>> actorClass;
	
	private String id;
	private String title;
	
	public String getId() {
		return id;
	}
	
	public String getTitle() {
		return title;
	}
	
	boolean isProxy() {
		return Proxy.isProxyClass(actorClass);				
	}

	ActorResult(Class<? extends Actor<WebDriver>> actorClass, String title) {
		this.actorClass = actorClass;		
		this.title = title;
	}

//	ActorResult(Class<? extends Actor<WebDriver>> actorClass) {
//		this(actorClass, actorClass.getAnnotation(Title.class)==null ? null : actorClass.getAnnotation(Title.class).value());	
//	}

	ActorResult(Class<? extends Actor<WebDriver>> actorClass, String title, String id) {
		this(actorClass, title);
		this.id = id;		
	}
	
	List<ActorMethodResult> results = new ArrayList<>();
	
	public List<ActorMethodResult> getResults() {
		return results;
	}
	
	public Class<? extends Actor<WebDriver>> getActorClass() {
		return actorClass;
	}
	
	public String getActorKey() {
		return !isProxy() || title==null || title.trim().length()==0 ? getActorInterface().getName() : getActorInterface().getName()+":"+title;
	}
	
	@SuppressWarnings("unchecked")
	public Class<? extends Actor<WebDriver>> getActorInterface() {
		if (actorClass.isInterface()) {
			return actorClass;
		}
		for (Class<?> i: actorClass.getInterfaces()) {
			if (Actor.class.isAssignableFrom(i)) {
				return (Class<? extends Actor<WebDriver>>) i;
			}
		}
		return actorClass;
	}
	
	public void merge(ActorResult anotherResult) {
		results.addAll(anotherResult.getResults());
		if (anotherResult.getId()!=null) {
			id = anotherResult.getId();
		}
	}
	
	public Map<Method, Integer> getCoverage() {
		Map<Method, Integer> ret = new HashMap<>();
		for (Method m: getActorInterface().getMethods()) {
			if (!Actor.class.equals(m.getDeclaringClass())) {
				int counter = 0;
				for (ActorMethodResult r: results) {
					if (m.equals(r.getOperation())) {
						++counter;
					}
				}
				
				ret.put(m, counter);
			}
		}
		return ret;
	}	
	
	
	@Override
	public void publish(
			URL url, 
			String securityToken, 
			boolean publishPerformance,
			Map<Object, String> idMap, 
			PublishMonitor monitor) throws Exception {
		
		if (monitor!=null) {
			monitor.onPublishing("Actor result "+getActorInterface().getName(), url);
		}
		HttpURLConnection pConnection = (HttpURLConnection) url.openConnection();
		pConnection.setRequestMethod("POST");
		pConnection.setDoOutput(true);
		pConnection.setRequestProperty("Authorization", "Bearer "+securityToken);
		JSONObject data = new JSONObject();
		WebTestUtil.qualifiedNameAndTitleAndDescriptionToJSON(getActorInterface(), data);
		if (getTitle()!=null) {
			data.put("title", getTitle());
		}
		data.put("isProxy", isProxy());		
		if (isProxy()) {
			data.put("qualifiedName", getActorKey());
		}
		JSONArray resultIDs = new JSONArray();
		data.put("results", resultIDs);
		for (OperationResult<?> r: getResults()) {
			String rId = idMap.get(r);
			if (rId==null) {
				throw new IllegalStateException("Operation result is not yet published");
			}
			resultIDs.put(rId);
		}
		JSONArray coverage = new JSONArray();
		data.put("coverage", coverage);
		for (Entry<Method, Integer> ce: getCoverage().entrySet()) {
			JSONObject coverageEntry = new JSONObject();
			coverage.put(coverageEntry);
			WebTestUtil.titleAndDescriptionToJSON(ce.getKey(), coverageEntry);
			if (!coverageEntry.has("title")) {
				coverageEntry.put("title", WebTestUtil.title(ce.getKey().getName()));
			}
			coverageEntry.put("qualifiedName", ce.getKey().toString());
			coverageEntry.put("invocations", ce.getValue());
		}
		try (Writer w = new OutputStreamWriter(pConnection.getOutputStream())) {
			data.write(w);
		}
		int responseCode = pConnection.getResponseCode();
		if (responseCode==HttpURLConnection.HTTP_OK) {
			id = pConnection.getHeaderField("ID");
			idMap.put(this, id);
		} else {
			throw new PublishException(url+" error: "+responseCode+" "+pConnection.getResponseMessage());
		}
	}

	@Override
	public int publishSize() {
		return 1;
	}				

}
