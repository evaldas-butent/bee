package com.butent.bee.shared.css.values;

import com.butent.bee.shared.css.HasCssName;

public enum Speak implements HasCssName {
  AUTO {
    @Override
    public String getCssName() {
      return "auto";
    }
  },
  NONE {
    @Override
    public String getCssName() {
      return "none";
    }
  },
  NORMAL {
    @Override
    public String getCssName() {
      return "normal";
    }
  }
}
