package com.butent.bee.shared.css.values;

import com.butent.bee.shared.css.HasCssName;

public enum MarqueeStyle implements HasCssName {
  SCROLL {
    @Override
    public String getCssName() {
      return "scroll";
    }
  },
  SLIDE {
    @Override
    public String getCssName() {
      return "slide";
    }
  },
  ALTERNATE {
    @Override
    public String getCssName() {
      return "alternate";
    }
  }
}
