package com.butent.bee.client.widget;

import com.google.gwt.user.client.ui.Frame;

import com.butent.bee.client.dom.DomUtils;
import com.butent.bee.shared.HasId;

public class BeeFrame extends Frame implements HasId {

  public BeeFrame(String url) {
    super(url);
    init();
  }

  public void createId() {
    DomUtils.createId(this, "frame");
  }

  public String getId() {
    return DomUtils.getId(this);
  }

  public void setId(String id) {
    DomUtils.setId(this, id);
  }

  private void init() {
    createId();
    setStyleName("bee-Frame");
  }
}
