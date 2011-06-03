package com.butent.bee.client.grid;

import com.google.gwt.i18n.client.NumberFormat;
import com.google.gwt.user.cellview.client.Column;

import com.butent.bee.client.i18n.Format;
import com.butent.bee.shared.data.IsRow;

/**
 * Implements row id column, enables to get value for a specified row.
 */

public class RowIdColumn extends Column<IsRow, Long> {
  
  public RowIdColumn() {
    this(Format.getDefaultLongFormat());
  }

  public RowIdColumn(NumberFormat format) {
    super(new NumberCell<Long>(format));
    setHorizontalAlignment(ALIGN_RIGHT);
  }
  
  @Override
  public Long getValue(IsRow row) {
    if (row == null) {
      return null;
    }
    return row.getId();
  }
}
