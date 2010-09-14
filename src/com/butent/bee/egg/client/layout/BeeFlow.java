package com.butent.bee.egg.client.layout;

import com.google.gwt.user.client.ui.FlowPanel;

import com.butent.bee.egg.client.utils.BeeDom;
import com.butent.bee.egg.shared.HasId;

public class BeeFlow extends FlowPanel implements HasId {

  public BeeFlow() {
    createId();
  }

  public void createId() {
    BeeDom.createId(this, "flow");
  }

  public String getId() {
    return BeeDom.getId(this);
  }

  public void setId(String id) {
    BeeDom.setId(this, id);
  }

}
