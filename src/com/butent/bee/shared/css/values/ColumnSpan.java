package com.butent.bee.shared.css.values;

import com.google.gwt.dom.client.Style.HasCssName;

public enum ColumnSpan implements HasCssName {
  NONE {
    @Override
    public String getCssName() {
      return "none";
    }
  },
  ALL {
    @Override
    public String getCssName() {
      return "all";
    }
  }
}
