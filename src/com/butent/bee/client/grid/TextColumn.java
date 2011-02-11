package com.butent.bee.client.grid;

import com.google.gwt.cell.client.Cell;
import com.google.gwt.user.cellview.client.Column;

import com.butent.bee.shared.BeeConst;
import com.butent.bee.shared.data.IsRow;
import com.butent.bee.shared.utils.BeeUtils;

public class TextColumn extends Column<IsRow, String> {
  private int idx;
  private int maxDisplaySize;

  public TextColumn(Cell<String> cell, int idx) {
    this(cell, idx, -1);
  }

  public TextColumn(Cell<String> cell, int idx, int max) {
    super(cell);
    this.idx = idx;
    this.maxDisplaySize = max;
  }
  
  public int getIdx() {
    return idx;
  }

  public int getMaxDisplaySize() {
    return maxDisplaySize;
  }

  @Override
  public String getValue(IsRow row) {
    String v = row.getString(idx);
    if (v == null) {
      return BeeConst.STRING_EMPTY;
    }
    if (maxDisplaySize <= 0 || v.length() <= maxDisplaySize) {
      return v;
    }
    
    return BeeUtils.clip(v, maxDisplaySize);
  }

  public void setIdx(int idx) {
    this.idx = idx;
  }

  public void setMaxDisplaySize(int maxDisplaySize) {
    this.maxDisplaySize = maxDisplaySize;
  }
}
