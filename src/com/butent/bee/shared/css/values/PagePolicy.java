package com.butent.bee.shared.css.values;

import com.google.gwt.dom.client.Style.HasCssName;

public enum PagePolicy implements HasCssName {
  START {
    @Override
    public String getCssName() {
      return "start";
    }
  },
  FIRST {
    @Override
    public String getCssName() {
      return "first";
    }
  },
  LAST {
    @Override
    public String getCssName() {
      return "last";
    }
  }
}
