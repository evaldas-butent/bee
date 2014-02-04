package com.butent.bee.shared.css.values;

import com.butent.bee.shared.css.HasCssName;

public enum FontVariantPosition implements HasCssName {
  NORMAL {
    @Override
    public String getCssName() {
      return "normal";
    }
  },
  SUB {
    @Override
    public String getCssName() {
      return "sub";
    }
  },
  SUPER {
    @Override
    public String getCssName() {
      return "super";
    }
  }
}
