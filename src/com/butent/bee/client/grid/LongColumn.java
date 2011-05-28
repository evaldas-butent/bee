package com.butent.bee.client.grid;

import com.google.gwt.i18n.client.NumberFormat;

import com.butent.bee.client.i18n.Format;
import com.butent.bee.shared.data.IsColumn;
import com.butent.bee.shared.data.IsRow;

public class LongColumn extends AbstractNumberColumn<Long> {
  
  public LongColumn(int index, IsColumn dataColumn) {
    this(Format.getDefaultLongFormat(), index, dataColumn);
  }

  public LongColumn(NumberFormat format, int index, IsColumn dataColumn) {
    super(format, index, dataColumn);
  }

  @Override
  protected Long getValue(IsRow row, int colIndex) {
    return row.getLong(colIndex);
  }
}
