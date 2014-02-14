package com.butent.bee.shared.css.values;

import com.butent.bee.shared.css.HasCssName;

public enum BreakInside implements HasCssName {
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
  AVOID_PAGE {
    @Override
    public String getCssName() {
      return "avoid-page";
    }
  },
  AVOID_COLUMN {
    @Override
    public String getCssName() {
      return "avoid-column";
    }
  }
}
