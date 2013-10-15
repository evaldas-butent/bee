package com.butent.bee.client.grid;

import com.butent.bee.client.view.grid.CellGrid;
import com.butent.bee.shared.data.IsRow;

/**
 * Enables to get information about cell's column, row and grid.
 */

public class CellContext {

  private final CellGrid grid;
  private final IsRow rowValue;
  private final int columnIndex;

  public CellContext(CellGrid grid, int columnIndex) {
    this(grid, null, columnIndex);
  }

  public CellContext(CellGrid grid, IsRow rowValue, int columnIndex) {
    this.grid = grid;
    this.rowValue = rowValue;
    this.columnIndex = columnIndex;
  }

  public int getColumnIndex() {
    return columnIndex;
  }

  public CellGrid getGrid() {
    return grid;
  }

  public IsRow getRowValue() {
    return rowValue;
  }
}
