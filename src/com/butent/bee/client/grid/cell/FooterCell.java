package com.butent.bee.client.grid.cell;

import com.google.gwt.cell.client.AbstractCell;
import com.google.gwt.core.client.GWT;
import com.google.gwt.dom.client.Element;
import com.google.gwt.safehtml.client.SafeHtmlTemplates;
import com.google.gwt.safehtml.shared.SafeHtml;
import com.google.gwt.safehtml.shared.SafeHtmlBuilder;

import com.butent.bee.client.dom.DomUtils;
import com.butent.bee.client.event.EventUtils;
import com.butent.bee.client.style.StyleUtils;
import com.butent.bee.client.view.search.AbstractFilterSupplier;
import com.butent.bee.shared.utils.BeeUtils;

public class FooterCell extends AbstractCell<AbstractFilterSupplier> {

  public interface Template extends SafeHtmlTemplates {
    @Template("<div id=\"{0}\" class=\"{1}\" title=\"{2}\">{3}</div>")
    SafeHtml content(String id, String classes, String title, String html);
  }

  private static final Template TEMPLATE = GWT.create(Template.class);
  
  private static final String STYLE_NAME = "bee-FooterCell";
  private static final String STYLE_EMPTY = STYLE_NAME + "-empty";
  private static final String STYLE_FILTER = STYLE_NAME + "-filter";
  
  private final String contentId;

  public FooterCell() {
    super(EventUtils.EVENT_TYPE_CLICK);

    this.contentId = DomUtils.createUniqueId("fc");
  }

  @Override
  public void render(Context context, AbstractFilterSupplier fs, SafeHtmlBuilder sb) {
    String html = BeeUtils.trim(fs.getDisplayHtml());
    String title = BeeUtils.trim(fs.getDisplayTitle());

    String classes = StyleUtils.buildClasses(STYLE_NAME, 
        html.isEmpty() ? STYLE_EMPTY : STYLE_FILTER);
    
    sb.append(TEMPLATE.content(contentId, classes, title, html));
  }
  
  public void update(Element container, AbstractFilterSupplier fs) {
    Element content = (container == null) ? null : DomUtils.getChildById(container, contentId);
    if (content == null) {
      content = DomUtils.getElement(contentId);
    }
    
    String html = fs.getDisplayHtml();
    
    boolean wasEmpty = BeeUtils.isEmpty(content.getInnerHTML());
    content.setInnerHTML(BeeUtils.trim(html));
    boolean isEmpty = BeeUtils.isEmpty(html);
    
    if (wasEmpty != isEmpty) {
      content.removeClassName(wasEmpty ? STYLE_EMPTY : STYLE_FILTER);
      content.addClassName(isEmpty ? STYLE_EMPTY : STYLE_FILTER);
    }
    
    content.setTitle(BeeUtils.trim(fs.getDisplayTitle()));
  }
}
