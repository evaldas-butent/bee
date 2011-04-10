package com.butent.bee.client.grid;

import com.google.gwt.user.cellview.client.Header;

public class ColumnFooter extends Header<String> {

  public ColumnFooter() {
    super(new FooterCell());
  }

  @Override
  public String getValue() {
    return null;
  }
}
