package com.butent.bee.shared.css.values;

import com.google.gwt.dom.client.Style.HasCssName;

public enum FontKerning implements HasCssName {
  AUTO {
    @Override
    public String getCssName() {
      return "auto";
    }
  },
  NORMAL {
    @Override
    public String getCssName() {
      return "normal";
    }
  },
  NONE {
    @Override
    public String getCssName() {
      return "none";
    }
  }
}
