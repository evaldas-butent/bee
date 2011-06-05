package com.butent.bee.client.grid;

import com.google.gwt.cell.client.Cell;
import com.google.gwt.cell.client.Cell.Context;
import com.google.gwt.user.cellview.client.Column;

import com.butent.bee.shared.data.IsRow;
import com.butent.bee.shared.data.value.ValueType;

public abstract class AbstractColumn<C> extends Column<IsRow, C> {

  public AbstractColumn(Cell<C> cell) {
    super(cell);
  }

  public abstract String getString(Context context, IsRow row);

  @Override
  public abstract C getValue(IsRow row);
  
  public abstract ValueType getValueType();
}
