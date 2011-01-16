package com.butent.bee.egg.shared.data;

import com.butent.bee.egg.shared.data.column.AbstractColumn;

public class SortInfo {
  private AbstractColumn column;
  private SortOrder order;

  public SortInfo(AbstractColumn column, SortOrder order) {
    this.column = column;
    this.order = order;
  }

  public AbstractColumn getColumn() {
    return column;
  }

  public SortOrder getOrder() {
    return order;
  }
  
  public String toQueryString() {
    return column.toQueryString() + (order == SortOrder.DESCENDING ? " DESC" : "");   
  }
}
