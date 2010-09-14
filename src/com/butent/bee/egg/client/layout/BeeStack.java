package com.butent.bee.egg.client.layout;

import com.google.gwt.dom.client.Style.Unit;
import com.google.gwt.user.client.ui.StackLayoutPanel;

import com.butent.bee.egg.client.utils.BeeDom;
import com.butent.bee.egg.shared.HasId;

public class BeeStack extends StackLayoutPanel implements HasId {

  public BeeStack(Unit unit) {
    super(unit);
    createId();
  }

  public void createId() {
    BeeDom.createId(this, "stack");
  }

  public String getId() {
    return BeeDom.getId(this);
  }

  public void setId(String id) {
    BeeDom.setId(this, id);
  }

}
