package com.butent.bee.client.layout;

import com.google.gwt.user.client.DOM;
import com.google.gwt.user.client.ui.ComplexPanel;
import com.google.gwt.user.client.ui.InsertPanel;
import com.google.gwt.user.client.ui.Widget;

import com.butent.bee.client.dom.DomUtils;
import com.butent.bee.client.ui.IdentifiableWidget;

/**
 * Enables to manage span elements from Document Object Model.
 */

public class Span extends ComplexPanel implements InsertPanel, IdentifiableWidget {

  public Span() {
    setElement(DOM.createSpan());
    DomUtils.createId(this, getIdPrefix());
    setStyleName(getDefaultStyleName());
  }

  @Override
  public void add(Widget w) {
    super.add(w, getElement());
  }

  public String getDefaultStyleName() {
    return "bee-Span";
  }

  @Override
  public String getId() {
    return DomUtils.getId(this);
  }

  @Override
  public String getIdPrefix() {
    return "span";
  }

  @Override
  public void insert(Widget w, int beforeIndex) {
    insert(w, getElement(), beforeIndex, true);
  }

  @Override
  public void setId(String id) {
    DomUtils.setId(this, id);
  }
}
