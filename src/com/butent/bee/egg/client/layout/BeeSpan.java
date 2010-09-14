package com.butent.bee.egg.client.layout;

import com.google.gwt.user.client.DOM;
import com.google.gwt.user.client.ui.ComplexPanel;
import com.google.gwt.user.client.ui.InsertPanel;
import com.google.gwt.user.client.ui.Widget;

import com.butent.bee.egg.client.utils.BeeDom;
import com.butent.bee.egg.shared.HasId;

public class BeeSpan extends ComplexPanel implements InsertPanel, HasId {

  public BeeSpan() {
    setElement(DOM.createSpan());
    createId();
  }

  @Override
  public void add(Widget w) {
    super.add(w, getElement());
  }

  public void createId() {
    BeeDom.createId(this, "span");
  }

  public String getId() {
    return BeeDom.getId(this);
  }

  public void insert(Widget w, int beforeIndex) {
    insert(w, getElement(), beforeIndex, true);
  }

  public void setId(String id) {
    BeeDom.setId(this, id);
  }

}
