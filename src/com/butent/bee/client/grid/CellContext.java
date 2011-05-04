package com.butent.bee.client.grid;

import com.google.gwt.cell.client.Cell.Context;

import com.butent.bee.client.view.grid.CellGrid;

public class CellContext extends Context {

  private final CellGrid grid;

  public CellContext(int index, int column, Object key, CellGrid grid) {
    super(index, column, key);
    this.grid = grid;
  }

  public CellGrid getGrid() {
    return grid;
  }
}
