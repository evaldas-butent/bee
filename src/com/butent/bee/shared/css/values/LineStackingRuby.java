package com.butent.bee.shared.css.values;

import com.google.gwt.dom.client.Style.HasCssName;

public enum LineStackingRuby implements HasCssName {
  EXCLUDE_RUBY {
    @Override
    public String getCssName() {
      return "exclude-ruby";
    }
  },
  INCLUDE_RUBY {
    @Override
    public String getCssName() {
      return "include-ruby";
    }
  }
}
