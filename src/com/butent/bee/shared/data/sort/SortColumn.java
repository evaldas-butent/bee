package com.butent.bee.shared.data.sort;

import com.butent.bee.shared.data.column.AbstractColumn;

public class SortColumn {
  private AbstractColumn column;
  private SortOrder order;

  public SortColumn(AbstractColumn column) {
    this(column, SortOrder.ASCENDING);
  }

  public SortColumn(AbstractColumn column, SortOrder order) {
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
