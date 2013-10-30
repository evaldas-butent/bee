package com.butent.bee.shared.css.values;

import com.google.gwt.dom.client.Style.HasCssName;

public enum TextDecorationLine implements HasCssName {
  NONE {
    @Override
    public String getCssName() {
      return "none";
    }
  },
  UNDERLINE {
    @Override
    public String getCssName() {
      return "underline";
    }
  },
  OVERLINE {
    @Override
    public String getCssName() {
      return "overline";
    }
  },
  LINE_THROUGH {
    @Override
    public String getCssName() {
      return "line-through";
    }
  },
  BLINK {
    @Override
    public String getCssName() {
      return "blink";
    }
  }
}
