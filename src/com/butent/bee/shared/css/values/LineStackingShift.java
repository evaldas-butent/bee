package com.butent.bee.shared.css.values;

import com.butent.bee.shared.css.HasCssName;

public enum LineStackingShift implements HasCssName {
  CONSIDER_SHIFTS {
    @Override
    public String getCssName() {
      return "consider-shifts";
    }
  },
  DISREGARD_SHIFTS {
    @Override
    public String getCssName() {
      return "disregard-shifts";
    }
  }
}
