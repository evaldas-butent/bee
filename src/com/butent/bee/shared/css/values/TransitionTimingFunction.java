package com.butent.bee.shared.css.values;

import com.butent.bee.shared.css.HasCssName;

public enum TransitionTimingFunction implements HasCssName {
  EASE {
    @Override
    public String getCssName() {
      return "ease";
    }
  },
  LINEAR {
    @Override
    public String getCssName() {
      return "linear";
    }
  },
  EASE_IN {
    @Override
    public String getCssName() {
      return "ease-in";
    }
  },
  EASE_OUT {
    @Override
    public String getCssName() {
      return "ease-out";
    }
  },
  EASE_IN_OUT {
    @Override
    public String getCssName() {
      return "ease-in-out";
    }
  },
  STEP_START {
    @Override
    public String getCssName() {
      return "step-start";
    }
  },
  STEP_END {
    @Override
    public String getCssName() {
      return "step-end";
    }
  }
}
