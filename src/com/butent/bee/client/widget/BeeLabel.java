package com.butent.bee.client.widget;

import com.google.gwt.dom.client.Document;
import com.google.gwt.dom.client.Element;
import com.google.gwt.user.client.ui.Label;

import com.butent.bee.client.dom.DomUtils;
import com.butent.bee.client.ui.IdentifiableWidget;

/**
 * Implements standard label user interface component.
 */

public class BeeLabel extends Label implements IdentifiableWidget {

  public BeeLabel() {
    super();
    init();
  }
  
  public BeeLabel(boolean inline) {
    this(inline ? Document.get().createSpanElement() : Document.get().createDivElement());
  }

  public BeeLabel(Element element) {
    super(element);
    init();
  }

  public BeeLabel(String text) {
    this();
    setText(text);
    init();
  }

  public BeeLabel(HorizontalAlignmentConstant align) {
    this();
    setHorizontalAlignment(align);
  }

  public BeeLabel(String text, boolean wordWrap) {
    this(text);
    setWordWrap(wordWrap);
    init();
  }

  public BeeLabel(String text, HorizontalAlignmentConstant align) {
    this(text);
    setHorizontalAlignment(align);
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

  private void init() {
    DomUtils.createId(this, getIdPrefix());
    setStyleName("bee-Label");
  }
}
