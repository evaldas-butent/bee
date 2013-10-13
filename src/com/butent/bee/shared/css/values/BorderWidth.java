package com.butent.bee.shared.css.values;

import com.google.gwt.dom.client.Style.HasCssName;

public enum BorderWidth implements HasCssName {
  THIN {
    @Override
    public String getCssName() {
      return "thin";
    }
  },
  MEDIUM {
    @Override
    public String getCssName() {
      return "medium";
    }
  },
  THICK {
    @Override
    public String getCssName() {
      return "thick";
    }
  }
}
