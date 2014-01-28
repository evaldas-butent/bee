package com.butent.bee.shared.css.values;

import com.butent.bee.shared.css.HasCssName;

public enum PunctuationTrim implements HasCssName {
  NONE {
    @Override
    public String getCssName() {
      return "none";
    }
  },
  START {
    @Override
    public String getCssName() {
      return "start";
    }
  },
  END {
    @Override
    public String getCssName() {
      return "end";
    }
  },
  ALLOW_END {
    @Override
    public String getCssName() {
      return "allow-end";
    }
  },
  ADJACENT {
    @Override
    public String getCssName() {
      return "adjacent";
    }
  }
}
