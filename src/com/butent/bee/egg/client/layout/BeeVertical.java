package com.butent.bee.egg.client.layout;

import com.google.gwt.user.client.ui.VerticalPanel;

import com.butent.bee.egg.client.utils.BeeDom;
import com.butent.bee.egg.shared.HasId;

public class BeeVertical extends VerticalPanel implements HasId {

  public BeeVertical() {
    createId();
  }

  public void createId() {
    BeeDom.createId(this, "vert");
  }

  public String getId() {
    return BeeDom.getId(this);
  }

  public void setId(String id) {
    BeeDom.setId(this, id);
  }

}
