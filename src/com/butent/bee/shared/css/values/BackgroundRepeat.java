package com.butent.bee.shared.css.values;

import com.google.gwt.dom.client.Style.HasCssName;

public enum BackgroundRepeat implements HasCssName {
  REPEAT_X {
    @Override
    public String getCssName() {
      return "repeat-x";
    }
  },
  REPEAT_Y {
    @Override
    public String getCssName() {
      return "repeat-y";
    }
  },
  REPEAT {
    @Override
    public String getCssName() {
      return "repeat";
    }
  },
  SPACE {
    @Override
    public String getCssName() {
      return "space";
    }
  },
  ROUND {
    @Override
    public String getCssName() {
      return "round";
    }
  },
  NO_REPEAT {
    @Override
    public String getCssName() {
      return "no-repeat";
    }
  }
}
