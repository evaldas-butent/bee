package com.butent.bee.egg.client.layout;

import com.google.gwt.user.client.ui.LayoutPanel;

import com.butent.bee.egg.client.utils.BeeDom;
import com.butent.bee.egg.shared.HasId;

public class BeeLayoutPanel extends LayoutPanel implements HasId {

  public BeeLayoutPanel() {
    createId();
  }

  public void createId() {
    BeeDom.createId(this, "layout");
  }

  public String getId() {
    return BeeDom.getId(this);
  }

  public void setId(String id) {
    BeeDom.setId(this, id);
  }

}
