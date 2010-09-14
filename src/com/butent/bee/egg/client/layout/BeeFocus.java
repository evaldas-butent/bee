package com.butent.bee.egg.client.layout;

import com.google.gwt.user.client.ui.FocusPanel;
import com.google.gwt.user.client.ui.Widget;

import com.butent.bee.egg.client.utils.BeeDom;
import com.butent.bee.egg.shared.HasId;

public class BeeFocus extends FocusPanel implements HasId {

  public BeeFocus() {
    super();
    createId();
  }

  public BeeFocus(Widget child) {
    super(child);
    createId();
  }

  public void createId() {
    BeeDom.createId(this, "focus");
  }

  public String getId() {
    return BeeDom.getId(this);
  }

  public void setId(String id) {
    BeeDom.setId(this, id);
  }

}
