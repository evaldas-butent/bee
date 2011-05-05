package com.butent.bee.client.layout;

import com.google.gwt.user.client.ui.SimplePanel;

import com.butent.bee.client.dom.DomUtils;
import com.butent.bee.shared.HasId;

/**
 * Contains a class for panels that contain only one widget.
 */

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
