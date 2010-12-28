package com.butent.bee.egg.server.datasource.query;

public class ColumnSort {
  private AbstractColumn column;
  private SortOrder order;

  public ColumnSort(AbstractColumn column, SortOrder order) {
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
