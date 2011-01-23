package com.butent.bee.shared.data.sort;

public class SortInfo {
  private int index;
  private SortOrder order;

  public SortInfo(int index) {
    this(index, SortOrder.ASCENDING);
  }

  public SortInfo(int index, SortOrder order) {
    this.index = index;
    this.order = order;
  }

  public int getIndex() {
    return index;
  }

  public SortOrder getOrder() {
    return order;
  }
  
  public boolean isAscending() {
    return order == SortOrder.ASCENDING;
  }
}
