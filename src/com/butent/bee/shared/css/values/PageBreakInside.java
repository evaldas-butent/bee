package com.butent.bee.shared.css.values;

import com.butent.bee.shared.css.HasCssName;

public enum PageBreakInside implements HasCssName {
  AUTO {
    @Override
    public String getCssName() {
      return "auto";
    }
  },
  AVOID {
    @Override
    public String getCssName() {
      return "avoid";
    }
  },
  INHERIT {
    @Override
    public String getCssName() {
      return "inherit";
    }
  }
}
