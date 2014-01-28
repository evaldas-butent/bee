package com.butent.bee.shared.css.values;

import com.butent.bee.shared.css.HasCssName;

public enum BorderImageRepeat implements HasCssName {
  STRETCH {
    @Override
    public String getCssName() {
      return "stretch";
    }
  },
  REPEAT {
    @Override
    public String getCssName() {
      return "repeat";
    }
  },
  ROUND {
    @Override
    public String getCssName() {
      return "round";
    }
  },
  SPACE {
    @Override
    public String getCssName() {
      return "space";
    }
  }
}
