package com.butent.bee.shared.css.values;

import com.butent.bee.shared.css.HasCssName;

public enum AlignContent implements HasCssName {
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
  SPACE_BETWEEN {
    @Override
    public String getCssName() {
      return "space-between";
    }
  },
  SPACE_AROUND {
    @Override
    public String getCssName() {
      return "space-around";
    }
  },
  STRETCH {
    @Override
    public String getCssName() {
      return "stretch";
    }
  }
}
