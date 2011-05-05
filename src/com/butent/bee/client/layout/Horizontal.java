package com.butent.bee.client.layout;

import com.google.gwt.user.client.ui.HorizontalPanel;

import com.butent.bee.client.dom.DomUtils;
import com.butent.bee.shared.HasId;

/**
 * Contains a panel that lays all of its widgets out in a single horizontal row.
 */

public class Horizontal extends HorizontalPanel implements HasId {

  public Horizontal() {
    createId();
  }

  public void createId() {
    DomUtils.createId(this, "hor");
  }

  public String getId() {
    return DomUtils.getId(this);
  }

  public void setId(String id) {
    DomUtils.setId(this, id);
  }

}
