package com.butent.bee.client.layout;

import com.google.gwt.user.client.ui.FlowPanel;

import com.butent.bee.client.dom.DomUtils;
import com.butent.bee.shared.HasId;

/**
 * Implements a panel that formats its child widgets using the default HTML layout behavior.
 */

public class Flow extends FlowPanel implements HasId {
  public Flow() {
    createId();
  }

  public void createId() {
    DomUtils.createId(this, "flow");
  }

  public String getId() {
    return DomUtils.getId(this);
  }

  public void setId(String id) {
    DomUtils.setId(this, id);
  }
}
