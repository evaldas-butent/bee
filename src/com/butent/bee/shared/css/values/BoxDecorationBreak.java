package com.butent.bee.shared.css.values;

import com.butent.bee.shared.css.HasCssName;

public enum BoxDecorationBreak implements HasCssName {
  SLICE {
    @Override
    public String getCssName() {
      return "slice";
    }
  },
  CLONE {
    @Override
    public String getCssName() {
      return "clone";
    }
  }
}
