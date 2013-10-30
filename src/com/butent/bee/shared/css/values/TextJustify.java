package com.butent.bee.shared.css.values;

import com.google.gwt.dom.client.Style.HasCssName;

public enum TextJustify implements HasCssName {
  AUTO {
    @Override
    public String getCssName() {
      return "auto";
    }
  },
  NONE {
    @Override
    public String getCssName() {
      return "none";
    }
  },
  INTER_WORD {
    @Override
    public String getCssName() {
      return "inter-word";
    }
  },
  DISTRIBUTE {
    @Override
    public String getCssName() {
      return "distribute";
    }
  }
}
