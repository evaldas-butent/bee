package com.butent.bee.shared.css.values;

import com.google.gwt.dom.client.Style.HasCssName;

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
