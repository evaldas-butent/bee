package com.butent.bee.shared.css.values;

import com.google.gwt.dom.client.Style.HasCssName;

public enum BackgroundSize implements HasCssName {
  AUTO {
    @Override
    public String getCssName() {
      return "auto";
    }
  },
  COVER {
    @Override
    public String getCssName() {
      return "cover";
    }
  },
  CONTAIN {
    @Override
    public String getCssName() {
      return "contain";
    }
  }
}
