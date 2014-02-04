package com.butent.bee.shared.css.values;

import com.butent.bee.shared.css.HasCssName;

public enum Hyphens implements HasCssName {
  NONE {
    @Override
    public String getCssName() {
      return "none";
    }
  },
  MANUAL {
    @Override
    public String getCssName() {
      return "manual";
    }
  },
  AUTO {
    @Override
    public String getCssName() {
      return "auto";
    }
  }
}
