package com.butent.bee.shared.css.values;

import com.butent.bee.shared.css.HasCssName;

public enum FontKerning implements HasCssName {
  AUTO {
    @Override
    public String getCssName() {
      return "auto";
    }
  },
  NORMAL {
    @Override
    public String getCssName() {
      return "normal";
    }
  },
  NONE {
    @Override
    public String getCssName() {
      return "none";
    }
  }
}
