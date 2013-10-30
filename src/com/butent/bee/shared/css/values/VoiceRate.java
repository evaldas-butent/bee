package com.butent.bee.shared.css.values;

import com.google.gwt.dom.client.Style.HasCssName;

public enum VoiceRate implements HasCssName {
  NORMAL {
    @Override
    public String getCssName() {
      return "normal";
    }
  },
  X_SLOW {
    @Override
    public String getCssName() {
      return "x-slow";
    }
  },
  SLOW {
    @Override
    public String getCssName() {
      return "slow";
    }
  },
  MEDIUM {
    @Override
    public String getCssName() {
      return "medium";
    }
  },
  FAST {
    @Override
    public String getCssName() {
      return "fast";
    }
  },
  X_FAST {
    @Override
    public String getCssName() {
      return "x-fast";
    }
  }
}
