package com.butent.bee.shared.css.values;

import com.butent.bee.shared.css.HasCssName;

public enum AnimationIterationCount implements HasCssName {
  INFINITE {
    @Override
    public String getCssName() {
      return "infinite";
    }
  }
}
