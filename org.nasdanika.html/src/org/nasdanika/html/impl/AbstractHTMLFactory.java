package org.nasdanika.html.impl;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicLong;

import org.nasdanika.html.Fragment;
import org.nasdanika.html.HTMLFactory;
import org.nasdanika.html.Select;
import org.nasdanika.html.Tag;

/**
 * This factory contains method implementations which are not dependent
 * on the underlying web framework.
 * @author Pavel
 *
 */
public abstract class AbstractHTMLFactory implements HTMLFactory {
	
	private static AtomicLong idCounter = new AtomicLong();
	
	@Override
	public String nextId() {
		return "nsd_"+Long.toString(idCounter.incrementAndGet(), Character.MAX_RADIX);
	}
	

	@Override
	public Tag tag(String tagName, final Object... content) {
		return new TagImpl(this, tagName, content);
	}

	@Override
	public Tag ul(final Iterable<?> items) {
		return new TagImpl(this, "ul") {
			
			@Override
			protected List<Object> getContent() {
				List<Object> ret = new ArrayList<>();
				for (Object item: items) {
					ret.add("<li>"+item+"</li>");
				}
				return ret;
			}
			
		};
	}

	@Override
	public Tag ol(final Iterable<?> items) {
		return new TagImpl(this, "ol") {
			
			@Override
			protected List<Object> getContent() {
				List<Object> ret = new ArrayList<>();
				for (Object item: items) {
					ret.add("<li>"+item+"</li>");
				}
				return ret;
			}
			
		};
	}
	
	@Override
	public Tag link(String href, final Object... content) {
		return new TagImpl(this, "a", content).attribute("href", href);
	}
	
	@Override
	public Tag routeLink(String targetElement, String path, Object... content) {
		return link("#router/"+(targetElement==null ? "main" : targetElement)+"/"+path, content);
	}
	
	@Override
	public Select select(final String name, final String id, final String placeholder) {
		return new SelectImpl(this)
			.attribute("name", name)
			.attribute("id", id)
			.attribute("placeholder", placeholder);
	}
	
	@Override
	public Fragment fragment(Object... content) {
		return new FragmentImpl(content);
	}
	
}