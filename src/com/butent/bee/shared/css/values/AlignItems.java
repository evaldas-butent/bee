package com.butent.bee.shared.css.values;

import com.google.gwt.dom.client.Style.HasCssName;

public enum AlignItems implements HasCssName {
  FLEX_START {
    @Override
    public String getCssName() {
      return "flex-start";
    }
  },
  FLEX_END {
    @Override
    public String getCssName() {
      return "flex-end";
    }
  },
  CENTER {
    @Override
    public String getCssName() {
      return "center";
    }
  },
  BASELINE {
    @Override
    public String getCssName() {
      return "baseline";
    }
  },
  STRETCH {
    @Override
    public String getCssName() {
      return "stretch";
    }
  }
}
