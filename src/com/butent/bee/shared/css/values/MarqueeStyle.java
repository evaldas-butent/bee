package com.butent.bee.shared.css.values;

import com.google.gwt.dom.client.Style.HasCssName;

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
