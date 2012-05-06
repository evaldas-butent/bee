package com.butent.bee.client.grid;

import com.google.gwt.cell.client.Cell.Context;

import com.butent.bee.client.view.grid.CellGrid;
import com.butent.bee.shared.data.IsRow;

/**
 * Enables to get information about cell's column, row and grid.
 */

public class CellContext extends Context {

  private final CellGrid grid;

  public CellContext(int index, int column, IsRow rowValue, CellGrid grid) {
    super(index, column, rowValue);
    this.grid = grid;
  }

  public CellGrid getGrid() {
    return grid;
  }
  
  public IsRow getRowValue() {
    if (getKey() instanceof IsRow) {
      return (IsRow) getKey();
    } else {
      return null;
    }
  }
}
