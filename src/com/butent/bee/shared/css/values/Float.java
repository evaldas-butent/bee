package com.butent.bee.shared.css.values;

import com.butent.bee.shared.css.HasCssName;

public enum Float implements HasCssName {
  LEFT {
    @Override
    public String getCssName() {
      return "left";
    }
  },
  RIGHT {
    @Override
    public String getCssName() {
      return "right";
    }
  },
  NONE {
    @Override
    public String getCssName() {
      return "none";
    }
  }
}
