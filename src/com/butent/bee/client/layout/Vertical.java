package com.butent.bee.client.layout;

import com.google.gwt.user.client.ui.VerticalPanel;

import com.butent.bee.client.dom.DomUtils;
import com.butent.bee.shared.HasId;

/**
 * Enables to use panel that lays all of its widgets out in a single vertical column.
 */

public class Vertical extends VerticalPanel implements HasId {

  public Vertical() {
    createId();
  }

  public void createId() {
    DomUtils.createId(this, "vert");
  }

  public String getId() {
    return DomUtils.getId(this);
  }

  public void setId(String id) {
    DomUtils.setId(this, id);
  }

}
