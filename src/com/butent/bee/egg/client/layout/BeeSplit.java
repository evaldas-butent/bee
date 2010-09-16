package com.butent.bee.egg.client.layout;

import com.google.gwt.user.client.ui.SplitLayoutPanel;

import com.butent.bee.egg.client.dom.DomUtils;
import com.butent.bee.egg.shared.HasId;

public class BeeSplit extends SplitLayoutPanel implements HasId {

  public BeeSplit() {
    createId();
  }

  public void createId() {
    DomUtils.createId(this, "split");
  }

  public String getId() {
    return DomUtils.getId(this);
  }

  public void setId(String id) {
    DomUtils.setId(this, id);
  }

}
