package com.butent.bee.client.grid.column;

import com.google.gwt.cell.client.Cell;
import com.google.gwt.cell.client.Cell.Context;

import com.butent.bee.shared.data.IsColumn;
import com.butent.bee.shared.data.IsRow;
import com.butent.bee.shared.data.value.ValueType;
import com.butent.bee.shared.ui.ColumnDescription.ColType;

/**
 * Is an abstract class for specific type implementing columns, requires them to have methods for
 * getting label, index and data column.
 */

public abstract class DataColumn<C> extends AbstractColumn<C> {

  private final int index;
  
  private final IsColumn dataColumn;

  public DataColumn(Cell<C> cell, int index, IsColumn dataColumn) {
    super(cell);
    this.index = index;
    this.dataColumn = dataColumn;
  }

  @Override
  public ColType getColType() {
    return ColType.DATA;
  }
  
  public IsColumn getDataColumn() {
    return dataColumn;
  }

  public int getIndex() {
    return index;
  }

  @Override
  public String getString(Context context, IsRow row) {
    if (row == null) {
      return null;
    }
    return row.getString(getIndex());
  }

  @Override
  public ValueType getValueType() {
    return getDataColumn().getType();
  }
}
