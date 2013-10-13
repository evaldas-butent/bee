package com.butent.bee.shared.css.values;

import com.google.gwt.dom.client.Style.HasCssName;

public enum AnimationPlayState implements HasCssName {
  RUNNING {
    @Override
    public String getCssName() {
      return "running";
    }
  },
  PAUSED {
    @Override
    public String getCssName() {
      return "paused";
    }
  }
}
