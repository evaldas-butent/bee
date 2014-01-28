package com.butent.bee.shared.css.values;

import com.butent.bee.shared.css.HasCssName;

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
