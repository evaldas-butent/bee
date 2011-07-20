package com.butent.bee.client.layout;

import com.google.gwt.dom.client.TableElement;
import com.google.gwt.user.client.ui.VerticalPanel;

import com.butent.bee.client.dom.DomUtils;
import com.butent.bee.shared.HasId;

/**
 * Enables to use panel that lays all of its widgets out in a single vertical column.
 */

public class Vertical extends VerticalPanel implements HasId {

  public Vertical() {
    DomUtils.createId(this, getIdPrefix());
  }

  public String getId() {
    return DomUtils.getId(this);
  }

  public String getIdPrefix() {
    return "vert";
  }

  public void setId(String id) {
    DomUtils.setId(this, id);
  }

  public void setPadding(int padding) {
    TableElement.as(getElement()).setCellPadding(padding);
  }
}
