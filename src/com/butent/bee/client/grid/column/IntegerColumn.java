package com.butent.bee.client.grid.column;

import com.google.gwt.i18n.client.NumberFormat;

import com.butent.bee.client.i18n.Format;
import com.butent.bee.shared.data.IsColumn;
import com.butent.bee.shared.data.IsRow;

/**
 * Enables using columns for integer type data.
 */

public class IntegerColumn extends NumberColumn<Integer> {

  public IntegerColumn(int index, IsColumn dataColumn) {
    this(Format.getDefaultIntegerFormat(), index, dataColumn);
  }

  public IntegerColumn(NumberFormat format, int index, IsColumn dataColumn) {
    super(format, index, dataColumn);
  }

  @Override
  protected Integer getValue(IsRow row, int colIndex) {
    return row.getInteger(colIndex);
  }
}
