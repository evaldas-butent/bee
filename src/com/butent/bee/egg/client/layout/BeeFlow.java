package com.butent.bee.egg.client.layout;

import com.google.gwt.user.client.ui.FlowPanel;

import com.butent.bee.egg.client.dom.DomUtils;
import com.butent.bee.egg.shared.HasId;

public class BeeFlow extends FlowPanel implements HasId {

  public BeeFlow() {
    createId();
  }

  public void createId() {
    DomUtils.createId(this, "flow");
  }

  public String getId() {
    return DomUtils.getId(this);
  }

  public void setId(String id) {
    DomUtils.setId(this, id);
  }

}
