package com.butent.bee.client.widget;

import com.google.gwt.dom.client.Document;
import com.google.gwt.dom.client.Element;

import com.butent.bee.client.dom.DomUtils;

/**
 * Implements standard label user interface component.
 */

public class Label extends CustomWidget {

  public Label() {
    this(false);
  }
  
  public Label(boolean inline) {
    this(inline ? Document.get().createSpanElement() : Document.get().createDivElement());
  }

  public Label(Element element) {
    super(element);
  }

  public Label(String text) {
    this();
    setText(text);
  }

  @Override
  public String getId() {
    return DomUtils.getId(this);
  }

  @Override
  public String getIdPrefix() {
    return "lbl";
  }
  
  @Override
  public void setId(String id) {
    DomUtils.setId(this, id);
  }

  @Override
  protected void init() {
    super.init();
    setStyleName("bee-Label");
  }
}
