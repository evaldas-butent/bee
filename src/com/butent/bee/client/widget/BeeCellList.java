package com.butent.bee.client.widget;

import com.google.gwt.cell.client.Cell;
import com.google.gwt.user.cellview.client.CellList;

import com.butent.bee.client.dom.DomUtils;
import com.butent.bee.shared.HasId;

/**
 * Implements a cell list user interface component, a single column list of cells.
 */

public class BeeCellList<T> extends CellList<T> implements HasId {

  public BeeCellList(Cell<T> cell) {
    super(cell);
    DomUtils.createId(this, getIdPrefix());
  }

  public String getId() {
    return DomUtils.getId(this);
  }

  public String getIdPrefix() {
    return "celllist";
  }

  public void setId(String id) {
    DomUtils.setId(this, id);
  }
}
