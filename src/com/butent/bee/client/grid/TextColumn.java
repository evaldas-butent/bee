package com.butent.bee.client.grid;

import com.google.gwt.cell.client.Cell;
import com.google.gwt.cell.client.TextCell;

import com.butent.bee.shared.data.IsRow;
import com.butent.bee.shared.utils.BeeUtils;

public class TextColumn extends CellColumn<String> {
  private int maxDisplaySize;

  public TextColumn(int index) {
    this(new TextCell(), index, -1);
  }

  public TextColumn(Cell<String> cell, int index) {
    this(cell, index, -1);
  }

  public TextColumn(Cell<String> cell, int index, int maxDisplaySize) {
    super(cell, index);
    this.maxDisplaySize = maxDisplaySize;
  }
  
  public int getMaxDisplaySize() {
    return maxDisplaySize;
  }

  @Override
  public String getValue(IsRow row) {
    String v = getString(row);
    if (maxDisplaySize <= 0 || v.length() <= maxDisplaySize) {
      return v;
    }
    return BeeUtils.clip(v, maxDisplaySize);
  }

  public void setMaxDisplaySize(int maxDisplaySize) {
    this.maxDisplaySize = maxDisplaySize;
  }
}
