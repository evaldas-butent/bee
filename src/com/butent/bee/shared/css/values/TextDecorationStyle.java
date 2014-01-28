package com.butent.bee.shared.css.values;

import com.butent.bee.shared.css.HasCssName;

public enum TextDecorationStyle implements HasCssName {
  SOLID {
    @Override
    public String getCssName() {
      return "solid";
    }
  },
  DOUBLE {
    @Override
    public String getCssName() {
      return "double";
    }
  },
  DOTTED {
    @Override
    public String getCssName() {
      return "dotted";
    }
  },
  DASHED {
    @Override
    public String getCssName() {
      return "dashed";
    }
  },
  WAVY {
    @Override
    public String getCssName() {
      return "wavy";
    }
  }
}
