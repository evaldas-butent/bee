package com.butent.bee.egg.client.layout;

import com.google.gwt.user.client.ui.HorizontalPanel;

import com.butent.bee.egg.client.dom.DomUtils;
import com.butent.bee.egg.shared.HasId;

public class BeeHorizontal extends HorizontalPanel implements HasId {

  public BeeHorizontal() {
    createId();
  }

  public void createId() {
    DomUtils.createId(this, "hor");
  }

  public String getId() {
    return DomUtils.getId(this);
  }

  public void setId(String id) {
    DomUtils.setId(this, id);
  }

}
