package com.butent.bee.shared.css.values;

import com.google.gwt.dom.client.Style.HasCssName;

public enum ListStylePosition implements HasCssName {
  INSIDE {
    @Override
    public String getCssName() {
      return "inside";
    }
  },
  HANGING {
    @Override
    public String getCssName() {
      return "hanging";
    }
  },
  OUTSIDE {
    @Override
    public String getCssName() {
      return "outside";
    }
  }
}
