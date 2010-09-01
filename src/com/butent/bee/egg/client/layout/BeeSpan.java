package com.butent.bee.egg.client.layout;

import com.butent.bee.egg.client.utils.BeeDom;
import com.butent.bee.egg.shared.HasId;
import com.google.gwt.user.client.DOM;
import com.google.gwt.user.client.ui.ComplexPanel;
import com.google.gwt.user.client.ui.InsertPanel;
import com.google.gwt.user.client.ui.Widget;

public class BeeSpan extends ComplexPanel implements InsertPanel, HasId {

  public BeeSpan() {
    setElement(DOM.createSpan());
    BeeDom.setId(this);
  }

  @Override
  public void add(Widget w) {
    super.add(w, getElement());
  }

  public void insert(Widget w, int beforeIndex) {
    insert(w, getElement(), beforeIndex, true);
  }

  public String getId() {
    return BeeDom.getId(this);
  }

  public void setId(String id) {
    BeeDom.setId(this, id);
  }

}
