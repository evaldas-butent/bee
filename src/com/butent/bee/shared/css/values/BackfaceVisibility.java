package com.butent.bee.shared.css.values;

import com.butent.bee.shared.css.HasCssName;

public enum BackfaceVisibility implements HasCssName {
  VISIBLE {
    @Override
    public String getCssName() {
      return "visible";
    }
  },
  HIDDEN {
    @Override
    public String getCssName() {
      return "hidden";
    }
  }
}
