package com.butent.bee.egg.client.layout;

import com.google.gwt.user.client.ui.HorizontalPanel;

import com.butent.bee.egg.client.utils.BeeDom;
import com.butent.bee.egg.shared.HasId;

public class BeeHorizontal extends HorizontalPanel implements HasId {

  public BeeHorizontal() {
    createId();
  }

  public void createId() {
    BeeDom.createId(this, "hor");
  }

  public String getId() {
    return BeeDom.getId(this);
  }

  public void setId(String id) {
    BeeDom.setId(this, id);
  }

}
