package com.butent.bee.shared.css.values;

import com.butent.bee.shared.css.HasCssName;

public enum BookmarkState implements HasCssName {
  OPEN {
    @Override
    public String getCssName() {
      return "open";
    }
  },
  CLOSED {
    @Override
    public String getCssName() {
      return "closed";
    }
  }
}
