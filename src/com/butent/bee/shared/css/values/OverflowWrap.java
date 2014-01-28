package com.butent.bee.shared.css.values;

import com.butent.bee.shared.css.HasCssName;

public enum OverflowWrap implements HasCssName {
  NORMAL {
    @Override
    public String getCssName() {
      return "normal";
    }
  },
  BREAK_WORD {
    @Override
    public String getCssName() {
      return "break-word";
    }
  }
}
