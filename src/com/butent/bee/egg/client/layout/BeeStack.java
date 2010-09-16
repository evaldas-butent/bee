package com.butent.bee.egg.client.layout;

import com.google.gwt.dom.client.Style.Unit;
import com.google.gwt.user.client.ui.StackLayoutPanel;

import com.butent.bee.egg.client.dom.DomUtils;
import com.butent.bee.egg.shared.HasId;

public class BeeStack extends StackLayoutPanel implements HasId {

  public BeeStack(Unit unit) {
    super(unit);
    createId();
  }

  public void createId() {
    DomUtils.createId(this, "stack");
  }

  public String getId() {
    return DomUtils.getId(this);
  }

  public void setId(String id) {
    DomUtils.setId(this, id);
  }

}
