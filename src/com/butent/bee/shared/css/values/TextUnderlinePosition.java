package com.butent.bee.shared.css.values;

import com.butent.bee.shared.css.HasCssName;

public enum TextUnderlinePosition implements HasCssName {
  AUTO {
    @Override
    public String getCssName() {
      return "auto";
    }
  },
  UNDER {
    @Override
    public String getCssName() {
      return "under";
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
  }
}
