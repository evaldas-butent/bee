package com.butent.bee.shared.css.values;

import com.google.gwt.dom.client.Style.HasCssName;

public enum Speak implements HasCssName {
  AUTO {
    @Override
    public String getCssName() {
      return "auto";
    }
  },
  NONE {
    @Override
    public String getCssName() {
      return "none";
    }
  },
  NORMAL {
    @Override
    public String getCssName() {
      return "normal";
    }
  }
}
