package com.butent.bee.client.layout;

import com.google.gwt.user.client.ui.ResizeLayoutPanel;

import com.butent.bee.client.dom.DomUtils;
import com.butent.bee.shared.HasId;

public class ResizePanel extends ResizeLayoutPanel implements HasId {

  public ResizePanel() {
    super();
    createId();
  }

  public void createId() {
    DomUtils.createId(this, "resizer");
  }

  public String getId() {
    return DomUtils.getId(this);
  }

  public void setId(String id) {
    DomUtils.setId(this, id);
  }
}
