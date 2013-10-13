package com.butent.bee.shared.css.values;

import com.google.gwt.dom.client.Style.HasCssName;

public enum FlexWrap implements HasCssName {
  NOWRAP {
    @Override
    public String getCssName() {
      return "nowrap";
    }
  },
  WRAP {
    @Override
    public String getCssName() {
      return "wrap";
    }
  },
  WRAP_REVERSE {
    @Override
    public String getCssName() {
      return "wrap-reverse";
    }
  }
}
