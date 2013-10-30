package com.butent.bee.shared.css.values;

import com.google.gwt.dom.client.Style.HasCssName;

public enum BaselineShift implements HasCssName {
  BASELINE {
    @Override
    public String getCssName() {
      return "baseline";
    }
  },
  SUB {
    @Override
    public String getCssName() {
      return "sub";
    }
  },
  SUPER {
    @Override
    public String getCssName() {
      return "super";
    }
  }
}
