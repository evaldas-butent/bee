package com.butent.bee.shared.css.values;

import com.google.gwt.dom.client.Style.HasCssName;

public enum Overflow implements HasCssName {
  VISIBLE {
    @Override
    public String getCssName() {
      return "visible";
    }
  },
  HIDDEN {
    @Override
    public String getCssName() {
      return "hidden";
    }
  },
  SCROLL {
    @Override
    public String getCssName() {
      return "scroll";
    }
  },
  AUTO {
    @Override
    public String getCssName() {
      return "auto";
    }
  },
  NO_DISPLAY {
    @Override
    public String getCssName() {
      return "no-display";
    }
  },
  NO_CONTENT {
    @Override
    public String getCssName() {
      return "no-content";
    }
  }
}
