package com.butent.bee.client.grid;

import com.google.gwt.i18n.client.NumberFormat;

import com.butent.bee.client.i18n.Format;
import com.butent.bee.shared.data.IsColumn;
import com.butent.bee.shared.data.IsRow;

/**
 * Enables using columns for {@code Double} type data.
 */

public class DoubleColumn extends NumberColumn<Double> {

  public DoubleColumn(int index, IsColumn dataColumn) {
    this(Format.getDefaultDoubleFormat(), index, dataColumn);
  }

  public DoubleColumn(NumberFormat format, int index, IsColumn dataColumn) {
    super(format, index, dataColumn);
  }

  @Override
  protected Double getValue(IsRow row, int colIndex) {
    return row.getDouble(colIndex);
  }
}
