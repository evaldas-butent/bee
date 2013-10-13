package com.butent.bee.shared.css.values;

import com.google.gwt.dom.client.Style.HasCssName;

public enum MarqueeSpeed implements HasCssName {
  SLOW {
    @Override
    public String getCssName() {
      return "slow";
    }
  },
  NORMAL {
    @Override
    public String getCssName() {
      return "normal";
    }
  },
  FAST {
    @Override
    public String getCssName() {
      return "fast";
    }
  }
}
