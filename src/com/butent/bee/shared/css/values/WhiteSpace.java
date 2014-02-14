package com.butent.bee.shared.css.values;

import com.butent.bee.shared.css.HasCssName;

public enum WhiteSpace implements HasCssName {
  NORMAL {
    @Override
    public String getCssName() {
      return "normal";
    }
  },
  PRE {
    @Override
    public String getCssName() {
      return "pre";
    }
  },
  NOWRAP {
    @Override
    public String getCssName() {
      return "nowrap";
    }
  },
  PRE_WRAP {
    @Override
    public String getCssName() {
      return "pre-wrap";
    }
  },
  PRE_LINE {
    @Override
    public String getCssName() {
      return "pre-line";
    }
  }
}
