package com.butent.bee.client.grid.property;

import java.util.HashMap;
import java.util.Map;

public class HeaderPropertyBase implements ColumnProperty {
  public static final int DEFAULT_ROW = 0;

  private int headerCount;
  private Map<Integer, Object> headers = new HashMap<Integer, Object>();
  private boolean isDynamic;

  public boolean isDynamic() {
    return isDynamic;
  }

  public void setDynamic(boolean isDynamic) {
    this.isDynamic = isDynamic;
  }

  Object getHeader(int row) {
    return headers.get(new Integer(row));
  }

  int getHeaderCount() {
    return headerCount;
  }

  void removeHeader(int row) {
    headers.remove(new Integer(row));
  }

  void setHeader(int row, Object header) {
    headers.put(new Integer(row), header);
    headerCount = Math.max(headerCount, row + 1);
  }

  void setHeaderCount(int headerCount) {
    this.headerCount = headerCount;
  }
}
