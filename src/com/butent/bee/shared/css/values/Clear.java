package com.butent.bee.shared.css.values;

import com.butent.bee.shared.css.HasCssName;

public enum Clear implements HasCssName {
  NONE {
    @Override
    public String getCssName() {
      return "none";
    }
  },
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
  BOTH {
    @Override
    public String getCssName() {
      return "both";
    }
  }
}
