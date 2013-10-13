package com.butent.bee.shared.css.values;

import com.google.gwt.dom.client.Style.HasCssName;

public enum InlineBoxAlign implements HasCssName {
  INITIAL {
    @Override
    public String getCssName() {
      return "initial";
    }
  },
  LAST {
    @Override
    public String getCssName() {
      return "last";
    }
  }
}
