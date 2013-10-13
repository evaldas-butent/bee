package com.butent.bee.shared.css.values;

import com.google.gwt.dom.client.Style.HasCssName;

public enum TextTransform implements HasCssName {
  NONE {
    @Override
    public String getCssName() {
      return "none";
    }
  },
  CAPITALIZE {
    @Override
    public String getCssName() {
      return "capitalize";
    }
  },
  UPPERCASE {
    @Override
    public String getCssName() {
      return "uppercase";
    }
  },
  LOWERCASE {
    @Override
    public String getCssName() {
      return "lowercase";
    }
  },
  FULL_WIDTH {
    @Override
    public String getCssName() {
      return "full-width";
    }
  }
}
