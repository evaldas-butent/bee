package com.butent.bee.shared.css.values;

import com.google.gwt.dom.client.Style.HasCssName;

public enum AnimationIterationCount implements HasCssName {
  INFINITE {
    @Override
    public String getCssName() {
      return "infinite";
    }
  }
}
