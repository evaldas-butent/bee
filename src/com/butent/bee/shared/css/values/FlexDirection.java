package com.butent.bee.shared.css.values;

import com.butent.bee.shared.css.HasCssName;

public enum FlexDirection implements HasCssName {
  ROW {
    @Override
    public String getCssName() {
      return "row";
    }
  },
  ROW_REVERSE {
    @Override
    public String getCssName() {
      return "row-reverse";
    }
  },
  COLUMN {
    @Override
    public String getCssName() {
      return "column";
    }
  },
  COLUMN_REVERSE {
    @Override
    public String getCssName() {
      return "column-reverse";
    }
  }
}
