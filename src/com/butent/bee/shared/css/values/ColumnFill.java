package com.butent.bee.shared.css.values;

import com.google.gwt.dom.client.Style.HasCssName;

public enum ColumnFill implements HasCssName {
  AUTO {
    @Override
    public String getCssName() {
      return "auto";
    }
  },
  BALANCE {
    @Override
    public String getCssName() {
      return "balance";
    }
  }
}
