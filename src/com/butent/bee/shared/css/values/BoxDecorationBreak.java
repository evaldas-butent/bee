package com.butent.bee.shared.css.values;

import com.google.gwt.dom.client.Style.HasCssName;

public enum BoxDecorationBreak implements HasCssName {
  SLICE {
    @Override
    public String getCssName() {
      return "slice";
    }
  },
  CLONE {
    @Override
    public String getCssName() {
      return "clone";
    }
  }
}
