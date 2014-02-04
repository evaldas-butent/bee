package com.butent.bee.shared.css.values;

import com.butent.bee.shared.css.HasCssName;

public enum AlignSelf implements HasCssName {
  AUTO {
    @Override
    public String getCssName() {
      return "auto";
    }
  },
  FLEX_START {
    @Override
    public String getCssName() {
      return "flex-start";
    }
  },
  FLEX_END {
    @Override
    public String getCssName() {
      return "flex-end";
    }
  },
  CENTER {
    @Override
    public String getCssName() {
      return "center";
    }
  },
  BASELINE {
    @Override
    public String getCssName() {
      return "baseline";
    }
  },
  STRETCH {
    @Override
    public String getCssName() {
      return "stretch";
    }
  }
}
