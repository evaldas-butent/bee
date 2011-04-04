package com.butent.bee.client.grid;

import com.google.gwt.cell.client.Cell;
import com.google.gwt.cell.client.TextCell;

import com.butent.bee.shared.data.IsRow;
import com.butent.bee.shared.utils.BeeUtils;

public class TextColumn extends CellColumn<String> {
  private int maxDisplaySize;

  public TextColumn(int index, String label) {
    this(new TextCell(), index, label, -1);
  }

  public TextColumn(Cell<String> cell, int index, String label) {
    this(cell, index, label, -1);
  }

  public TextColumn(Cell<String> cell, int index, String label, int maxDisplaySize) {
    super(cell, index, label);
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
