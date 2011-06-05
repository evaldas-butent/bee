package com.butent.bee.client.grid;

import com.google.gwt.cell.client.Cell.Context;
import com.google.gwt.i18n.client.NumberFormat;

import com.butent.bee.client.i18n.Format;
import com.butent.bee.client.i18n.HasNumberFormat;
import com.butent.bee.shared.data.IsRow;
import com.butent.bee.shared.data.value.ValueType;
import com.butent.bee.shared.utils.BeeUtils;

/**
 * Implements row id column, enables to get value for a specified row.
 */

public class RowIdColumn extends AbstractColumn<Long> implements HasNumberFormat {
  
  public RowIdColumn() {
    this(Format.getDefaultLongFormat());
  }

  public RowIdColumn(NumberFormat format) {
    super(new NumberCell<Long>(format));
    setHorizontalAlignment(ALIGN_RIGHT);
  }
  
  public NumberFormat getNumberFormat() {
    if (getCell() instanceof HasNumberFormat) {
      return ((HasNumberFormat) getCell()).getNumberFormat();
    }
    return null;
  }

  @Override
  public String getString(Context context, IsRow row) {
    if (row == null) {
      return null;
    }
    return BeeUtils.toString(row.getId());
  }

  @Override
  public Long getValue(IsRow row) {
    if (row == null) {
      return null;
    }
    return row.getId();
  }

  @Override
  public ValueType getValueType() {
    return ValueType.LONG;
  }

  public void setNumberFormat(NumberFormat format) {
    if (getCell() instanceof HasNumberFormat) {
      ((HasNumberFormat) getCell()).setNumberFormat(format);
    }
  }
}
