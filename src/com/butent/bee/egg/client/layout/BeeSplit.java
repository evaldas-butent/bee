package com.butent.bee.egg.client.layout;

import com.google.gwt.user.client.ui.SplitLayoutPanel;

import com.butent.bee.egg.client.utils.BeeDom;
import com.butent.bee.egg.shared.HasId;

public class BeeSplit extends SplitLayoutPanel implements HasId {

  public BeeSplit() {
    createId();
  }

  public void createId() {
    BeeDom.createId(this, "split");
  }

  public String getId() {
    return BeeDom.getId(this);
  }

  public void setId(String id) {
    BeeDom.setId(this, id);
  }

}
