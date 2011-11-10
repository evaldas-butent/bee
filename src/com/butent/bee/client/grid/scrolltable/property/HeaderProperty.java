package com.butent.bee.client.grid.scrolltable.property;

/**
 * Extends {@code HeaderPropertyBase} class, handles setting and getting column header and header
 * count.
 */

public class HeaderProperty extends HeaderPropertyBase {
  public static final String NAME = "header";

  @Override
  public Object getHeader(int row) {
    return super.getHeader(row);
  }

  @Override
  public int getHeaderCount() {
    return super.getHeaderCount();
  }

  @Override
  public void removeHeader(int row) {
    super.removeHeader(row);
  }

  @Override
  public void setHeader(int row, Object header) {
    super.setHeader(row, header);
  }

  @Override
  public void setHeaderCount(int headerCount) {
    super.setHeaderCount(headerCount);
  }
}
