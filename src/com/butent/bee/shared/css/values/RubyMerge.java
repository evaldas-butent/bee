package com.butent.bee.shared.css.values;

import com.google.gwt.dom.client.Style.HasCssName;

public enum RubyMerge implements HasCssName {
  SEPARATE {
    @Override
    public String getCssName() {
      return "separate";
    }
  },
  COLLAPSE {
    @Override
    public String getCssName() {
      return "collapse";
    }
  },
  AUTO {
    @Override
    public String getCssName() {
      return "auto";
    }
  }
}
