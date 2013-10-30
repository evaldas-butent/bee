package com.butent.bee.shared.css.values;

import com.google.gwt.dom.client.Style.HasCssName;

public enum BorderCollapse implements HasCssName {
  COLLAPSE {
    @Override
    public String getCssName() {
      return "collapse";
    }
  },
  SEPARATE {
    @Override
    public String getCssName() {
      return "separate";
    }
  },
  INHERIT {
    @Override
    public String getCssName() {
      return "inherit";
    }
  }
}
