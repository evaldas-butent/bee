package com.butent.bee.shared.css.values;

import com.google.gwt.dom.client.Style.HasCssName;

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
