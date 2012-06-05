package com.butent.bee.client.layout;

import com.google.gwt.dom.client.TableElement;
import com.google.gwt.user.client.ui.HorizontalPanel;

import com.butent.bee.client.dom.DomUtils;

/**
 * Contains a panel that lays all of its widgets out in a single horizontal row.
 */

public class Horizontal extends HorizontalPanel implements CellVector {

  public Horizontal() {
    DomUtils.createId(this, getIdPrefix());
  }

  public String getId() {
    return DomUtils.getId(this);
  }

  public String getIdPrefix() {
    return "hor";
  }

  public void setId(String id) {
    DomUtils.setId(this, id);
  }
  
  public void setPadding(int padding) {
    TableElement.as(getElement()).setCellPadding(padding);
  }
}
