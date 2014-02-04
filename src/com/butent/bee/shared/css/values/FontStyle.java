package com.butent.bee.shared.css.values;

import com.butent.bee.shared.css.HasCssName;

public enum FontStyle implements HasCssName {
  NORMAL {
    @Override
    public String getCssName() {
      return "normal";
    }
  },
  ITALIC {
    @Override
    public String getCssName() {
      return "italic";
    }
  },
  OBLIQUE {
    @Override
    public String getCssName() {
      return "oblique";
    }
  }
}
