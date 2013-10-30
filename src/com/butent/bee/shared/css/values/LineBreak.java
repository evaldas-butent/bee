package com.butent.bee.shared.css.values;

import com.google.gwt.dom.client.Style.HasCssName;

public enum LineBreak implements HasCssName {
  AUTO {
    @Override
    public String getCssName() {
      return "auto";
    }
  },
  LOOSE {
    @Override
    public String getCssName() {
      return "loose";
    }
  },
  NORMAL {
    @Override
    public String getCssName() {
      return "normal";
    }
  },
  STRICT {
    @Override
    public String getCssName() {
      return "strict";
    }
  }
}
