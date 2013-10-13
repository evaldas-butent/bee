package com.butent.bee.shared.css.values;

import com.google.gwt.dom.client.Style.HasCssName;

public enum EmptyCells implements HasCssName {
  SHOW {
    @Override
    public String getCssName() {
      return "show";
    }
  },
  HIDE {
    @Override
    public String getCssName() {
      return "hide";
    }
  },
  INHERIT {
    @Override
    public String getCssName() {
      return "inherit";
    }
  }
}
