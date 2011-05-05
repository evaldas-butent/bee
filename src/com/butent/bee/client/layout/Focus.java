package com.butent.bee.client.layout;

import com.google.gwt.user.client.ui.FocusPanel;

import com.butent.bee.client.dom.DomUtils;
import com.butent.bee.shared.HasId;

/**
 * Enables to use a panel that makes its contents focusable, and adds the ability to catch mouse and
 * keyboard events.
 */

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
