package com.butent.bee.shared.css.values;

import com.butent.bee.shared.css.HasCssName;

public enum InlineBoxAlign implements HasCssName {
  INITIAL {
    @Override
    public String getCssName() {
      return "initial";
    }
  },
  LAST {
    @Override
    public String getCssName() {
      return "last";
    }
  }
}
