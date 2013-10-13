package com.butent.bee.shared.css.values;

import com.google.gwt.dom.client.Style.HasCssName;

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
