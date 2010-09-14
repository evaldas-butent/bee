package com.butent.bee.egg.client.grid;

import com.google.gwt.user.cellview.client.TextColumn;

import com.butent.bee.egg.shared.data.BeeView;

public class BeeTextColumn extends TextColumn<Integer> {
  private BeeView view;
  private int idx;

  public BeeTextColumn() {
    super();
  }

  public BeeTextColumn(BeeView view, int idx) {
    this();
    this.view = view;
    this.idx = idx;
  }

  public int getIdx() {
    return idx;
  }

  @Override
  public String getValue(Integer row) {
    return view.getValue(row, idx);
  }

  public BeeView getView() {
    return view;
  }

  public void setIdx(int idx) {
    this.idx = idx;
  }

  public void setView(BeeView view) {
    this.view = view;
  }

}
