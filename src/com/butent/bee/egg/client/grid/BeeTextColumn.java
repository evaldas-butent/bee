package com.butent.bee.egg.client.grid;

import com.google.gwt.user.cellview.client.TextColumn;

import com.butent.bee.egg.shared.BeeConst;
import com.butent.bee.egg.shared.data.BeeView;
import com.butent.bee.egg.shared.utils.BeeUtils;

public class BeeTextColumn extends TextColumn<Integer> {
  private BeeView view;
  private int idx;
  private int maxDisplaySize;

  public BeeTextColumn(BeeView view, int idx) {
    this(view, idx, -1);
  }

  public BeeTextColumn(BeeView view, int idx, int max) {
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

  public BeeView getView() {
    return view;
  }

  public void setIdx(int idx) {
    this.idx = idx;
  }

  public void setMaxDisplaySize(int maxDisplaySize) {
    this.maxDisplaySize = maxDisplaySize;
  }

  public void setView(BeeView view) {
    this.view = view;
  }

}
