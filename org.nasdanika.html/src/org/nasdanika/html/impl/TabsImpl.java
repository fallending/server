package org.nasdanika.html.impl;

import java.util.ArrayList;
import java.util.List;

import org.nasdanika.html.HTMLFactory;
import org.nasdanika.html.Tabs;
import org.nasdanika.html.Tag;

class TabsImpl extends UIElementImpl<Tabs> implements Tabs {

	private String tabId;

	TabsImpl(HTMLFactory factory) {
		super(factory);
		tabId = factory.nextId()+"_tab";
	}
	
	private abstract class Tab implements AutoCloseable {
		int idx;
		Object name;
		
		@Override
		public void close() throws Exception {
			UIElementImpl.close(name);			
		}

		Tab(Object name, int idx) {
			super();
			this.name = name;
			this.idx = idx;
		}	
		
		Object li() {
			Tag ret = factory.tag("li");
			if (idx==0) {
				ret.addClass("active");
			}
			ret.content(link());			
			return ret;
		}
		
		Tag div() {
			Tag ret = factory.div(tabContent()).addClass("tab-pane").id(tabId+"_"+idx);
			if (idx==0) {
				ret.addClass("active");
			}
			return ret;
		}
		
		protected abstract Tag link();
		
		protected abstract Object[] tabContent();
		
	}
	
	private class ContentTab extends Tab {
		
		ContentTab(Object name, int idx, Object[] content) {
			super(name, idx);
			this.content = content;
		}

		Object[] content;
		
		@Override
		public void close() throws Exception {
			super.close();
			for (Object c: content) {
				UIElementImpl.close(c);
			}
		}

		@Override
		protected Tag link() {
			Tag link = factory.link("#"+tabId+"_"+idx, name).attribute("data-toggle", "tab");
			return link;
		}
		
		@Override
		protected Object[] tabContent() {
			return content;
		}
	}
	
	private class AjaxTab extends Tab {
		
		AjaxTab(Object name, int idx, Object location) {
			super(name, idx);
			this.location = location;
		}

		Object location;
		
		@Override
		public void close() throws Exception {
			super.close();
			UIElementImpl.close(location);
		}

		@Override
		protected Tag link() {
			Tag link = factory.link(location.toString(), name).attribute("data-target", "#"+tabId+"_"+idx).attribute("data-toggle", "tabajax");
			return link;
		}
		
		@Override
		protected Object[] tabContent() {
			return new Object[] {""};
		}
		
		@Override
		Tag div() {
			Tag ret = super.div();
			if (idx==0) {
				ret.remoteContent(location.toString()); // Loading active tab
			}
			return ret;
		}
	}
	
	private List<Tab> tabs = new ArrayList<>();

	@Override
	public void close() throws Exception {
		super.close();
		for (Tab tab: tabs) {
			tab.close();
		}		
	}

	@Override
	public Tabs item(Object name, Object... content) {
		tabs.add(new ContentTab(name, tabs.size(), content));
		return this;
	}

	@Override
	public Tabs ajaxItem(Object name, Object location) {
		tabs.add(new AjaxTab(name, tabs.size(), location));
		return this;
	}
	
	private TabAjaxDataToggleScriptRenderer tabAjaxDataToggleScriptRenderer = new TabAjaxDataToggleScriptRenderer();
	
	public String toString() {
		if (isEmpty()) {
			return "";
		}
				
		Tag navUL = factory.tag("ul").addClass("nav").addClass("nav-tabs");		
		boolean hasAjaxTabs = false;
		for (Tab tab: tabs) {
			navUL.content(tab.li());
			hasAjaxTabs = hasAjaxTabs || tab instanceof AjaxTab;
		}
		Tag contentDiv = factory.div().addClass("tab-content");
		for (Tab tab: tabs) {
			contentDiv.content(tab.div());
		}
		StringBuilder sb = new StringBuilder(renderComment()).append("<div").append(attributes()).append(">");		
		sb.append(navUL);
		sb.append(contentDiv);
		if (hasAjaxTabs) {
			sb.append(tabAjaxDataToggleScriptRenderer.generate(null));
		}		
		return sb.append("</div>").append(genLoadRemoteContentScript()).toString();
	};

	@Override
	public boolean isEmpty() {
		return tabs.isEmpty();
	}

}

