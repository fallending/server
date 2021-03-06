package org.nasdanika.cdo.web.html;

import java.util.Map;

import org.apache.commons.lang3.StringEscapeUtils;
import org.nasdanika.cdo.EAttributeClosure;
import org.nasdanika.core.Converter;
import org.nasdanika.html.HTMLFactory;
import org.nasdanika.html.Table;
import org.nasdanika.web.HttpServletRequestContext;
import org.nasdanika.web.html.HTMLRenderer;

public class EAttributeClosureToHTMLRendererConverter implements Converter<EAttributeClosure<?>, HTMLRenderer, HttpServletRequestContext> {

	@Override
	public void close() throws Exception {
		// TODO Auto-generated method stub
		
	}

	@Override
	public HTMLRenderer convert(final EAttributeClosure<?> source, Class<HTMLRenderer> target, HttpServletRequestContext context) throws Exception {
		return new HTMLRenderer() {
			
			@Override
			public String render(HttpServletRequestContext context, String profile, Map<String, Object> environment) throws Exception {
				if ("label".equals(profile)) {
					return StringEscapeUtils.escapeHtml4(source.getFeature().getName());
				}
				if (source.getFeature().isMany()) {
					Table data = context.adapt(HTMLFactory.class).table().bordered();
					for (Object e: (Iterable<?>) source.getValue()) {
						data.row().cell(context.toHTML(e, "label", null));						
					}
					return data.toString();
				}
				return context.toHTML(source.getValue(), null, null);
			}
			
		};
	}


}
