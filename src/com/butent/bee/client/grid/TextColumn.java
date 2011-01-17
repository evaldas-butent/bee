package com.butent.bee.client.grid;

import com.google.gwt.cell.client.Cell;
import com.google.gwt.user.cellview.client.Column;

import com.butent.bee.shared.BeeConst;
import com.butent.bee.shared.data.HasTabularData;
import com.butent.bee.shared.utils.BeeUtils;

public class TextColumn extends Column<Integer, String> {
  private HasTabularData view;
  private int idx;
  private int maxDisplaySize;

  public TextColumn(Cell<String> cell, HasTabularData view, int idx) {
    this(cell, view, idx, -1);
  }

  public TextColumn(Cell<String> cell, HasTabularData view, int idx, int max) {
    super(cell);

    this.view = view;
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
  public String getValue(Integer row) {
    String v = view.getValue(row, idx);
    if (v == null) {
      return BeeConst.STRING_EMPTY;
    }
    if (maxDisplaySize <= 0 || v.length() <= maxDisplaySize) {
      return v;
    }
    
    return BeeUtils.clip(v, maxDisplaySize);
  }

  public HasTabularData getView() {
    return view;
  }

  public void setIdx(int idx) {
    this.idx = idx;
  }

  public void setMaxDisplaySize(int maxDisplaySize) {
    this.maxDisplaySize = maxDisplaySize;
  }

  public void setView(HasTabularData view) {
    this.view = view;
  }
}
