package org.nasdanika.cdo.web.doc;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.atomic.AtomicLong;
import java.util.function.Predicate;

import org.apache.commons.lang3.StringEscapeUtils;
import org.json.JSONArray;
import org.json.JSONObject;
import org.nasdanika.core.CoreUtil;
import org.nasdanika.html.HTMLFactory;
import org.nasdanika.html.Tag;
import org.nasdanika.html.Tag.TagName;
import org.nasdanika.html.UIElement.Event;

public class TocNode {	
	
	public interface TocNodeVisitor {
		
		void visit(TocNode tocNode);
		
	}

	private String text;
	private String href;
	private String icon;
	private TocNode parent;
	private List<TocNode> children = new CopyOnWriteArrayList<>();
	private AtomicLong counter;
	private String id;
	private String content;
	private String tocId;
	private Predicate<Object> predicate;
	private boolean hidden; 
	private String tooltip;
			
	public String getContent() {
		return content;
	}
	
	public void setContent(String content) {
		this.content = content;
	}
	
	public String getHref() {
		return href;
	}
	
	public void setHref(String href) {
		this.href = href;
	}
	
	public String getText() {
		return text;
	}
	
	public String getIcon() {
		return icon;
	}
	
	public String getId() {
		return id;
	}
	
	public String getTooltip() {
		return tooltip;
	}
	
	public void setTooltip(String title) {
		this.tooltip = title;
	}
	
	protected TocNode(
			String text, 
			String href, 
			String icon, 
			String tooltip,
			AtomicLong counter,
			String tocId,
			Predicate<Object> objectPredicate,
			boolean hidden) {
		
		this.text = text;
		this.href = href;
		this.icon = icon;
		this.tooltip = tooltip;
		this.counter = counter;
		this.id = "content_node_"+Long.toString(counter.incrementAndGet(), Character.MAX_RADIX);
		this.tocId = tocId;
		if (CoreUtil.isBlank(this.href)) {
			if (CoreUtil.isBlank(this.tocId)) {
				this.href = "/toc/"+this.id;
			} else {
				this.href = "/toc/"+this.tocId;				
			}
		}
		this.predicate = objectPredicate == null ? obj->false : objectPredicate; 
		this.hidden = hidden;
	}
		
	public TocNode(
			String text, 
			String href, 
			String icon, 
			String tooltip,
			Predicate<Object> objectPredicate, 
			boolean hidden) {
		this(text, href, icon, tooltip, new AtomicLong(), null, objectPredicate, hidden);
	}

	public List<TocNode> getChildren() {
		return children;
	}
	
	public TocNode createChild(
			String text, 
			String href, 
			String icon, 
			String tooltip,
			String tocId, 
			Predicate<Object> objectPredicate,
			boolean hidden) {
		TocNode child = new TocNode(text, href, icon, tooltip, counter, tocId, objectPredicate, hidden);
		children.add(child);
		child.parent = this;
		return child;
	}
	
	public TocNode match(Object obj) {
		if (predicate.test(obj)) {
			return this;
		}
		for (TocNode child: getChildren()) {
			TocNode matchedChild = child.match(obj);
			if (matchedChild != null) {
				return matchedChild;
			}
		}
		return null;
	}
	
	public List<TocNode> getPath() {
		List<TocNode> ret = parent==null ? new ArrayList<TocNode>() : parent.getPath();
		ret.add(this);
		return ret;
	}
	
	public TocNode getParent() {
		return parent;
	}
	
	public TocNode find(String href) {
		if (this.href!=null && this.href.equals(href)) {
			return this;
		}
		for (TocNode child: children) {
			TocNode ret = child.find(href);
			if (ret!=null) {
				return ret;
			}
		}
		return null;
	}
	
	public TocNode findByTocId(String tocId) {
		if (this.tocId!=null && this.tocId.equals(tocId)) {
			return this;
		}
		for (TocNode child: children) {
			TocNode ret = child.findByTocId(tocId);
			if (ret!=null) {
				return ret;
			}
		}
		return null;		
	}
	
	public JSONObject toJSON(String contextURL) throws Exception {
		JSONObject ret = new JSONObject();
		if (text!=null) {
			ret.put("text", text);
		}
		if (icon!=null) {
			ret.put("icon", contextURL+icon);
		}
		ret.put("id", id);
		JSONArray jsonChildren = new JSONArray();
		for (TocNode child: children) {
			if (!child.isHidden()) {
				JSONObject jsonChild = child.toJSON(contextURL);
				if (jsonChild!=null) {
					jsonChildren.put(jsonChild);
				}
			}
		}
		if (jsonChildren.length()>0) {
			ret.put("children", jsonChildren);
		}
		
		JSONObject aAttributes = new JSONObject();
		if (tooltip != null) {
			aAttributes.put("title", StringEscapeUtils.escapeHtml4(tooltip));
		}
		if (!aAttributes.keySet().isEmpty()) {
			ret.put("a_attr", aAttributes);
		}
		return ret;
	}

	void accept(TocNodeVisitor visitor) {
		visitor.visit(this);
		for (TocNode child: children) {
			child.accept(visitor);
		}
	}
	
	public boolean isHidden() {
		return hidden || (parent != null && parent.isHidden());
	}
	
	public static final Comparator<TocNode> NAME_COMPARATOR = new Comparator<TocNode>() {

		@Override
		public int compare(TocNode o1, TocNode o2) {
			if (o1==null || o1.getText()==null) {
				if (o2==null || o2.getText()==null) {
					return 0;
				}
				return 1;
			}
			if (o2==null || o2.getText()==null) {
				return -1;
			}
			return o1.getText().compareTo(o2.getText());
		}
	};	

	public void sort(boolean recursive) {
		Collections.sort(children, NAME_COMPARATOR);
		if (recursive) {
			for (TocNode child: children) {
				child.sort(recursive);
			}
		}
	}
	
	public Tag getLink(String docRoutePath) {
		String iconTag = CoreUtil.isBlank(getIcon()) ? "" : HTMLFactory.INSTANCE.tag(TagName.img).attribute("src", docRoutePath+getIcon()).style().margin().right("3px").style("vertical-align", "text-top").toString();
		StringBuilder path = new StringBuilder();
		for (TocNode pe: getPath()) {
			if (path.length() > 0) {
				path.append(" / ");
			}
			if (pe.getText() != null) {
				path.append(pe.getText());
			}
		}
		
		if (isHidden()) {
			return HTMLFactory.INSTANCE.link(DocRoute.ROUTER_DOC_CONTENT_FRAGMENT_PREFIX+docRoutePath+href, iconTag, StringEscapeUtils.escapeHtml4(getText())).attribute("title", path);
			
		}
		return HTMLFactory.INSTANCE.link("#", iconTag, StringEscapeUtils.escapeHtml4(getText())).attribute("title", path).on(Event.click, DocRoute.tocNodeSelectScript(getId()));
	}
}
