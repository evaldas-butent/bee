package com.butent.bee.shared.css.values;

import com.google.gwt.dom.client.Style.HasCssName;

public enum RubyPosition implements HasCssName {
  OVER {
    @Override
    public String getCssName() {
      return "over";
    }
  },
  UNDER {
    @Override
    public String getCssName() {
      return "under";
    }
  },
  INTER_CHARACTER {
    @Override
    public String getCssName() {
      return "inter-character";
    }
  },
  RIGHT {
    @Override
    public String getCssName() {
      return "right";
    }
  },
  LEFT {
    @Override
    public String getCssName() {
      return "left";
    }
  }
}
