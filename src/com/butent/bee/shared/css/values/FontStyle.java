package com.butent.bee.shared.css.values;

import com.google.gwt.dom.client.Style.HasCssName;

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
