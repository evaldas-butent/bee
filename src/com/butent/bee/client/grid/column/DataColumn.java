package com.butent.bee.client.grid.column;

import com.google.gwt.cell.client.Cell;
import com.google.gwt.cell.client.Cell.Context;

import com.butent.bee.shared.data.CellSource;
import com.butent.bee.shared.data.IsRow;
import com.butent.bee.shared.data.value.ValueType;
import com.butent.bee.shared.ui.ColumnDescription.ColType;

public abstract class DataColumn<C> extends AbstractColumn<C> {

  private final CellSource cellSource;

  public DataColumn(Cell<C> cell, CellSource cellSource) {
    super(cell);
    this.cellSource = cellSource;
  }

  @Override
  public ColType getColType() {
    return ColType.DATA;
  }
  
  @Override
  public String getString(Context context, IsRow row) {
    if (row == null) {
      return null;
    }
    return cellSource.getString(row);
  }

  @Override
  public ValueType getValueType() {
    return cellSource.getValueType();
  }

  protected CellSource getCellSource() {
    return cellSource;
  }
}
