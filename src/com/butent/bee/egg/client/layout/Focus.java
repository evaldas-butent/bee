package com.butent.bee.egg.client.layout;

import com.google.gwt.user.client.ui.FocusPanel;

import com.butent.bee.egg.client.dom.DomUtils;
import com.butent.bee.egg.shared.HasId;

public class Focus extends FocusPanel implements HasId {
  public Focus() {
    createId();
  }

  public void createId() {
    DomUtils.createId(this, "focus");
  }

  public String getId() {
    return DomUtils.getId(this);
  }

  public void setId(String id) {
    DomUtils.setId(this, id);
  }
}
