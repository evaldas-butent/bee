package com.butent.bee.client.widget;

import com.google.gwt.dom.client.Element;
import com.google.gwt.safehtml.shared.SafeHtml;
import com.google.gwt.user.client.ui.InlineHTML;

import com.butent.bee.client.dom.DomUtils;
import com.butent.bee.shared.HasId;

/**
 * Implements a user interface component that can contain arbitrary HTML and uses a span element,
 * causing it to be displayed with inline layout.
 */

public class InlineHtml extends InlineHTML implements HasId {

  public InlineHtml() {
    super();
    init();
  }

  public InlineHtml(Element element) {
    super(element);
    init();
  }

  public InlineHtml(SafeHtml html) {
    super(html);
    init();
  }

  public InlineHtml(String html) {
    super(html);
    init();
  }

  public String getId() {
    return DomUtils.getId(this);
  }

  public String getIdPrefix() {
    return "inline";
  }

  public void setId(String id) {
    DomUtils.setId(this, id);
  }

  private void init() {
    DomUtils.createId(this, getIdPrefix());
    setWordWrap(false);
    setStyleName("bee-InlineHtml");
  }
}
