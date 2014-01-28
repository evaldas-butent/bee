package com.butent.bee.shared.css.values;

import com.butent.bee.shared.css.HasCssName;

public enum HangingPunctuation implements HasCssName {
  NONE {
    @Override
    public String getCssName() {
      return "none";
    }
  },
  FIRST {
    @Override
    public String getCssName() {
      return "first";
    }
  },
  FORCE_END {
    @Override
    public String getCssName() {
      return "force-end";
    }
  },
  ALLOW_END {
    @Override
    public String getCssName() {
      return "allow-end";
    }
  },
  LAST {
    @Override
    public String getCssName() {
      return "last";
    }
  }
}
