package com.butent.bee.egg.client.layout;

import com.google.gwt.user.client.ui.VerticalPanel;

import com.butent.bee.egg.client.dom.DomUtils;
import com.butent.bee.egg.shared.HasId;

public class Vertical extends VerticalPanel implements HasId {

  public Vertical() {
    createId();
  }

  public void createId() {
    DomUtils.createId(this, "vert");
  }

  public String getId() {
    return DomUtils.getId(this);
  }

  public void setId(String id) {
    DomUtils.setId(this, id);
  }

}
