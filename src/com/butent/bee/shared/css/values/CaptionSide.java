package com.butent.bee.shared.css.values;

import com.butent.bee.shared.css.HasCssName;

public enum CaptionSide implements HasCssName {
  TOP {
    @Override
    public String getCssName() {
      return "top";
    }
  },
  BOTTOM {
    @Override
    public String getCssName() {
      return "bottom";
    }
  },
  INHERIT {
    @Override
    public String getCssName() {
      return "inherit";
    }
  }
}
