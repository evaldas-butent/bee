package com.butent.bee.shared.css.values;

import com.butent.bee.shared.css.HasCssName;

public enum WordBreak implements HasCssName {
  NORMAL {
    @Override
    public String getCssName() {
      return "normal";
    }
  },
  KEEP_ALL {
    @Override
    public String getCssName() {
      return "keep-all";
    }
  },
  BREAK_ALL {
    @Override
    public String getCssName() {
      return "break-all";
    }
  }
}
