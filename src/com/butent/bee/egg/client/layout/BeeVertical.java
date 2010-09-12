package com.butent.bee.egg.client.layout;

import com.butent.bee.egg.client.utils.BeeDom;
import com.butent.bee.egg.shared.HasId;
import com.google.gwt.user.client.ui.VerticalPanel;

public class BeeVertical extends VerticalPanel implements HasId {

  public BeeVertical() {
    createId();
  }

  public String getId() {
    return BeeDom.getId(this);
  }

  public void setId(String id) {
    BeeDom.setId(this, id);
  }

  public void createId() {
    BeeDom.createId(this, "vert");
  }

}
