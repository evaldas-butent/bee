package com.butent.bee.shared.css.values;

import com.butent.bee.shared.css.HasCssName;

public enum Visibility implements HasCssName {
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
  },
  COLLAPSE {
    @Override
    public String getCssName() {
      return "collapse";
    }
  }
}
