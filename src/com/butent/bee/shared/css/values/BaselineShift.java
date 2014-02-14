package com.butent.bee.shared.css.values;

import com.butent.bee.shared.css.HasCssName;

public enum BaselineShift implements HasCssName {
  BASELINE {
    @Override
    public String getCssName() {
      return "baseline";
    }
  },
  SUB {
    @Override
    public String getCssName() {
      return "sub";
    }
  },
  SUPER {
    @Override
    public String getCssName() {
      return "super";
    }
  }
}
