package com.butent.bee.shared.css.values;

import com.google.gwt.dom.client.Style.HasCssName;

public enum VoiceBalance implements HasCssName {
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
  LEFTWARDS {
    @Override
    public String getCssName() {
      return "leftwards";
    }
  },
  RIGHTWARDS {
    @Override
    public String getCssName() {
      return "rightwards";
    }
  }
}
