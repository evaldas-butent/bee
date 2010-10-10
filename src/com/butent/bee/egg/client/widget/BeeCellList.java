package com.butent.bee.egg.client.widget;

import com.google.gwt.cell.client.Cell;
import com.google.gwt.user.cellview.client.CellList;

import com.butent.bee.egg.client.dom.DomUtils;
import com.butent.bee.egg.shared.HasId;

public class BeeCellList<T> extends CellList<T> implements HasId {

  public BeeCellList(Cell<T> cell) {
    super(cell);
    createId();
  }

  public void createId() {
    DomUtils.createId(this, "celllist");
  }

  public String getId() {
    return DomUtils.getId(this);
  }

  public void setId(String id) {
    DomUtils.setId(this, id);
  }

}
