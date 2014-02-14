package com.butent.bee.shared.css.values;

import com.butent.bee.shared.css.HasCssName;

public enum BackgroundAttachment implements HasCssName {
  SCROLL {
    @Override
    public String getCssName() {
      return "scroll";
    }
  },
  FIXED {
    @Override
    public String getCssName() {
      return "fixed";
    }
  },
  LOCAL {
    @Override
    public String getCssName() {
      return "local";
    }
  }
}
