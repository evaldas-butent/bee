package com.butent.bee.client.grid;

import com.google.gwt.i18n.client.NumberFormat;

import com.butent.bee.client.i18n.Format;
import com.butent.bee.shared.data.IsColumn;
import com.butent.bee.shared.data.IsRow;

import java.math.BigDecimal;

/**
 * Enables using columns for decimal numbers type of data.
 */

public class DecimalColumn extends NumberColumn<BigDecimal> {

  public DecimalColumn(int index, IsColumn dataColumn) {
    this(Format.getDecimalFormat(dataColumn.getScale()), index, dataColumn);
  }

  public DecimalColumn(NumberFormat format, int index, IsColumn dataColumn) {
    super(format, index, dataColumn);
  }

  @Override
  protected BigDecimal getValue(IsRow row, int colIndex) {
    return row.getDecimal(colIndex);
  }
}
