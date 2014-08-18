package org.nasdanika.webtest;

import java.lang.reflect.Method;

import org.nasdanika.html.HTMLFactory.Glyphicon;

/**
 * Contains results of actor method execution.
 * @author Pavel Vlasov
 *
 */
public class ActorMethodResult extends MethodResult {

	ActorMethodResult(String id, Method method, Object[] arguments, MethodResult parent) {
		super(id, method, arguments, parent);
	}
	
	Glyphicon getGlyphicon() {
		return Glyphicon.user;
	}

}