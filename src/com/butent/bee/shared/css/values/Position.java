package com.butent.bee.shared.css.values;

import com.butent.bee.shared.css.HasCssName;

public enum Position implements HasCssName {
  STATIC {
    @Override
    public String getCssName() {
      return "static";
    }
  },
  RELATIVE {
    @Override
    public String getCssName() {
      return "relative";
    }
  },
  ABSOLUTE {
    @Override
    public String getCssName() {
      return "absolute";
    }
  },
  FIXED {
    @Override
    public String getCssName() {
      return "fixed";
    }
  },
  INHERIT {
    @Override
    public String getCssName() {
      return "inherit";
    }
  }
}
