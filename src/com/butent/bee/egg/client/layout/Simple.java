package com.butent.bee.egg.client.layout;

import com.google.gwt.user.client.ui.SimplePanel;

import com.butent.bee.egg.client.dom.DomUtils;
import com.butent.bee.egg.shared.HasId;

public class Simple extends SimplePanel implements HasId {

  public Simple() {
    super();
    createId();
  }

  public void createId() {
    DomUtils.createId(this, "simple");
  }

  public String getId() {
    return DomUtils.getId(this);
  }

  public void setId(String id) {
    DomUtils.setId(this, id);
  }

}
