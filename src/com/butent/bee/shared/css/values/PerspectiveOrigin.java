package com.butent.bee.shared.css.values;

import com.google.gwt.dom.client.Style.HasCssName;

public enum PerspectiveOrigin implements HasCssName {
  LEFT {
    @Override
    public String getCssName() {
      return "left";
    }
  },
  CENTER {
    @Override
    public String getCssName() {
      return "center";
    }
  },
  RIGHT {
    @Override
    public String getCssName() {
      return "right";
    }
  },
  TOP {
    @Override
    public String getCssName() {
      return "top";
    }
  },
  BOTTOM {
    @Override
    public String getCssName() {
      return "bottom";
    }
  }
}
