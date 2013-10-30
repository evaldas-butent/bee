package com.butent.bee.shared.css.values;

import com.google.gwt.dom.client.Style.HasCssName;

public enum TextAlignLast implements HasCssName {
  AUTO {
    @Override
    public String getCssName() {
      return "auto";
    }
  },
  START {
    @Override
    public String getCssName() {
      return "start";
    }
  },
  END {
    @Override
    public String getCssName() {
      return "end";
    }
  },
  LEFT {
    @Override
    public String getCssName() {
      return "left";
    }
  },
  RIGHT {
    @Override
    public String getCssName() {
      return "right";
    }
  },
  CENTER {
    @Override
    public String getCssName() {
      return "center";
    }
  },
  JUSTIFY {
    @Override
    public String getCssName() {
      return "justify";
    }
  }
}
