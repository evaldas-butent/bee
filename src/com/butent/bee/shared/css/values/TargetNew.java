package com.butent.bee.shared.css.values;

import com.google.gwt.dom.client.Style.HasCssName;

public enum TargetNew implements HasCssName {
  WINDOW {
    @Override
    public String getCssName() {
      return "window";
    }
  },
  TAB {
    @Override
    public String getCssName() {
      return "tab";
    }
  },
  NONE {
    @Override
    public String getCssName() {
      return "none";
    }
  }
}
