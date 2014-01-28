package com.butent.bee.shared.css.values;

import com.butent.bee.shared.css.HasCssName;

public enum PresentationLevel implements HasCssName {
  SAME {
    @Override
    public String getCssName() {
      return "same";
    }
  },
  INCREMENT {
    @Override
    public String getCssName() {
      return "increment";
    }
  }
}
