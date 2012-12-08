package com.butent.bee.client.grid.column;

import com.google.gwt.cell.client.Cell;
import com.google.gwt.cell.client.TextCell;

import com.butent.bee.shared.BeeConst;
import com.butent.bee.shared.data.CellSource;
import com.butent.bee.shared.data.IsRow;
import com.butent.bee.shared.utils.BeeUtils;

/**
 * Implements text type column, enables to get value for a specified row and manage column's maximum
 * display size.
 */

public class TextColumn extends DataColumn<String> {
  private int maxDisplaySize;

  public TextColumn(CellSource cellSource) {
    this(new TextCell(), cellSource, BeeConst.UNDEF);
  }

  public TextColumn(Cell<String> cell, CellSource cellSource) {
    this(cell, cellSource, BeeConst.UNDEF);
  }

  public TextColumn(Cell<String> cell, CellSource cellSource, int maxDisplaySize) {
    super(cell, cellSource);
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
    
    String v = getCellSource().getString(row);
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
