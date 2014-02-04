package com.butent.bee.shared.css.values;

import com.butent.bee.shared.css.HasCssName;

public enum Pause implements HasCssName {
  NONE {
    @Override
    public String getCssName() {
      return "none";
    }
  },
  X_WEAK {
    @Override
    public String getCssName() {
      return "x-weak";
    }
  },
  WEAK {
    @Override
    public String getCssName() {
      return "weak";
    }
  },
  MEDIUM {
    @Override
    public String getCssName() {
      return "medium";
    }
  },
  STRONG {
    @Override
    public String getCssName() {
      return "strong";
    }
  },
  X_STRONG {
    @Override
    public String getCssName() {
      return "x-strong";
    }
  }
}
