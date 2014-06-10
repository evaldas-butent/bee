package com.butent.bee.client.grid;

import com.butent.bee.client.view.grid.CellGrid;
import com.butent.bee.shared.BeeConst;
import com.butent.bee.shared.data.IsRow;

/**
 * Enables to get information about cell's column, row and grid.
 */

public class CellContext {

  private final CellGrid grid;
  private IsRow row;
  private int columnIndex;

  public CellContext(CellGrid grid) {
    this(grid, null, BeeConst.UNDEF);
  }

  public CellContext(CellGrid grid, int columnIndex) {
    this(grid, null, columnIndex);
  }

  public CellContext(CellGrid grid, IsRow row, int columnIndex) {
    this.grid = grid;
    this.row = row;
    this.columnIndex = columnIndex;
  }

  public int getColumnIndex() {
    return columnIndex;
  }

  public CellGrid getGrid() {
    return grid;
  }

  public IsRow getRow() {
    return row;
  }

  public void setColumnIndex(int columnIndex) {
    this.columnIndex = columnIndex;
  }

  public void setRow(IsRow row) {
    this.row = row;
  }
}
