package com.butent.bee.client.grid;

import com.google.gwt.cell.client.Cell;
import com.google.gwt.cell.client.TextCell;

import com.butent.bee.shared.data.IsColumn;
import com.butent.bee.shared.data.IsRow;
import com.butent.bee.shared.utils.BeeUtils;

/**
 * Implements text type column, enables to get value for a specified row and manage column's maximum
 * display size.
 */

public class TextColumn extends CellColumn<String> {
  private int maxDisplaySize;

  public TextColumn(int index, IsColumn dataColumn) {
    this(new TextCell(), index, dataColumn, -1);
  }

  public TextColumn(Cell<String> cell, int index, IsColumn dataColumn) {
    this(cell, index, dataColumn, -1);
  }

  public TextColumn(Cell<String> cell, int index, IsColumn dataColumn, int maxDisplaySize) {
    super(cell, index, dataColumn);
    this.maxDisplaySize = maxDisplaySize;
  }

  public int getMaxDisplaySize() {
    return maxDisplaySize;
  }

  @Override
  public String getValue(IsRow row) {
    if (row == null) {
      return null;
    }
    String v = row.getString(getIndex());
    if (v == null) {
      return null;
    }

    if (maxDisplaySize <= 0 || v.length() <= maxDisplaySize) {
      return v;
    }
    return BeeUtils.clip(v, maxDisplaySize);
  }

  public void setMaxDisplaySize(int maxDisplaySize) {
    this.maxDisplaySize = maxDisplaySize;
  }
}
