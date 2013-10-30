package com.butent.bee.shared.css.values;

import com.google.gwt.dom.client.Style.HasCssName;

public enum TextHeight implements HasCssName {
  AUTO {
    @Override
    public String getCssName() {
      return "auto";
    }
  },
  FONT_SIZE {
    @Override
    public String getCssName() {
      return "font-size";
    }
  },
  TEXT_SIZE {
    @Override
    public String getCssName() {
      return "text-size";
    }
  },
  MAX_SIZE {
    @Override
    public String getCssName() {
      return "max-size";
    }
  }
}
