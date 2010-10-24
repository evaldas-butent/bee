package com.butent.bee.egg.client.pst;

public class FooterProperty extends HeaderPropertyBase {
  public static final Type<FooterProperty> TYPE = new Type<FooterProperty>() {
    private FooterProperty instance;

    @Override
    public FooterProperty getDefault() {
      if (instance == null) {
        instance = new FooterProperty();
      }
      return instance;
    }
  };

  public Object getFooter(int row) {
    return super.getHeader(row);
  }

  public int getFooterCount() {
    return super.getHeaderCount();
  }

  public void removeFooter(int row) {
    super.removeHeader(row);
  }

  public void setFooter(int row, Object footer) {
    super.setHeader(row, footer);
  }

  public void setFooterCount(int footerCount) {
    super.setHeaderCount(footerCount);
  }
}
