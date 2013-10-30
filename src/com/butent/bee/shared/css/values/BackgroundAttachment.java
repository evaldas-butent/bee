package com.butent.bee.shared.css.values;

import com.google.gwt.dom.client.Style.HasCssName;

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
