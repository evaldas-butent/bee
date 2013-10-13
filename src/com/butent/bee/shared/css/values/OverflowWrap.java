package com.butent.bee.shared.css.values;

import com.google.gwt.dom.client.Style.HasCssName;

public enum OverflowWrap implements HasCssName {
  NORMAL {
    @Override
    public String getCssName() {
      return "normal";
    }
  },
  BREAK_WORD {
    @Override
    public String getCssName() {
      return "break-word";
    }
  }
}
