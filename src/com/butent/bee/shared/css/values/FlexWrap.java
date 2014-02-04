package com.butent.bee.shared.css.values;

import com.butent.bee.shared.css.HasCssName;

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
