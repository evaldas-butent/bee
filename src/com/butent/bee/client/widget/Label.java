package com.butent.bee.client.widget;

import com.google.gwt.dom.client.Document;
import com.google.gwt.dom.client.Element;

import com.butent.bee.shared.BeeConst;

/**
 * Implements standard label user interface component.
 */

public class Label extends CustomHasHtml {

  public Label() {
    this(false);
  }
  
  public Label(boolean inline) {
    this(inline ? Document.get().createSpanElement() : Document.get().createDivElement());
  }

  public Label(Element element) {
    super(element);
  }

  public Label(String html) {
    this();
    setHtml(html);
  }
  
  public void clear() {
    setHtml(BeeConst.STRING_EMPTY);
  }

  @Override
  public String getIdPrefix() {
    return "lbl";
  }
  
  @Override
  protected void init() {
    super.init();
    addStyleName("bee-Label");
  }
}
