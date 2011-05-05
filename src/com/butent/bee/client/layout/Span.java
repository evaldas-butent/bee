package com.butent.bee.client.layout;

import com.google.gwt.user.client.DOM;
import com.google.gwt.user.client.ui.ComplexPanel;
import com.google.gwt.user.client.ui.InsertPanel;
import com.google.gwt.user.client.ui.Widget;

import com.butent.bee.client.dom.DomUtils;
import com.butent.bee.shared.HasId;

/**
 * Enables to manage span elements from Document Object Model.
 */

public class Span extends ComplexPanel implements InsertPanel, HasId {

  public Span() {
    setElement(DOM.createSpan());
    createId();
    setStyleName(getDefaultStyleName());
  }

  @Override
  public void add(Widget w) {
    super.add(w, getElement());
  }

  public void createId() {
    DomUtils.createId(this, "span");
  }

  public String getDefaultStyleName() {
    return "bee-Span";
  }

  public String getId() {
    return DomUtils.getId(this);
  }

  public void insert(Widget w, int beforeIndex) {
    insert(w, getElement(), beforeIndex, true);
  }

  public void setId(String id) {
    DomUtils.setId(this, id);
  }

}
