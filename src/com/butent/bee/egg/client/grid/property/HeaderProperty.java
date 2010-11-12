package com.butent.bee.egg.client.grid.property;

public class HeaderProperty extends HeaderPropertyBase {
  public static final Type<HeaderProperty> TYPE = new Type<HeaderProperty>() {
    private HeaderProperty instance;

    @Override
    public HeaderProperty getDefault() {
      if (instance == null) {
        instance = new HeaderProperty();
      }
      return instance;
    }
  };

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
