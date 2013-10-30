package com.butent.bee.shared.css.values;

import com.google.gwt.dom.client.Style.HasCssName;

public enum Resize implements HasCssName {
  NONE {
    @Override
    public String getCssName() {
      return "none";
    }
  },
  BOTH {
    @Override
    public String getCssName() {
      return "both";
    }
  },
  HORIZONTAL {
    @Override
    public String getCssName() {
      return "horizontal";
    }
  },
  VERTICAL {
    @Override
    public String getCssName() {
      return "vertical";
    }
  },
  INHERIT {
    @Override
    public String getCssName() {
      return "inherit";
    }
  }
}
