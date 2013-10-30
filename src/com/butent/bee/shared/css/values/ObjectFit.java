package com.butent.bee.shared.css.values;

import com.google.gwt.dom.client.Style.HasCssName;

public enum ObjectFit implements HasCssName {
  FILL {
    @Override
    public String getCssName() {
      return "fill";
    }
  },
  CONTAIN {
    @Override
    public String getCssName() {
      return "contain";
    }
  },
  COVER {
    @Override
    public String getCssName() {
      return "cover";
    }
  },
  NONE {
    @Override
    public String getCssName() {
      return "none";
    }
  },
  SCALE_DOWN {
    @Override
    public String getCssName() {
      return "scale-down";
    }
  }
}
