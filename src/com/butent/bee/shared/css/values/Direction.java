package com.butent.bee.shared.css.values;

import com.butent.bee.shared.css.HasCssName;

public enum Direction implements HasCssName {
  LTR {
    @Override
    public String getCssName() {
      return "ltr";
    }
  },
  RTL {
    @Override
    public String getCssName() {
      return "rtl";
    }
  },
  INHERIT {
    @Override
    public String getCssName() {
      return "inherit";
    }
  }
}
