package com.butent.bee.shared.css.values;

import com.google.gwt.dom.client.Style.HasCssName;

public enum Direction implements HasCssName {
  LTR {
    @Override
    public String getCssName() {
      return "ltr";
    }
  },
  RTL {
    @Override
    public String getCssName() {
      return "rtl";
    }
  },
  INHERIT {
    @Override
    public String getCssName() {
      return "inherit";
    }
  }
}
