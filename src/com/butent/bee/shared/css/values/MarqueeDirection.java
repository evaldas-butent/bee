package com.butent.bee.shared.css.values;

import com.butent.bee.shared.css.HasCssName;

public enum MarqueeDirection implements HasCssName {
  FORWARD {
    @Override
    public String getCssName() {
      return "forward";
    }
  },
  REVERSE {
    @Override
    public String getCssName() {
      return "reverse";
    }
  }
}
