package com.butent.bee.shared.css.values;

import com.google.gwt.dom.client.Style.HasCssName;

public enum Pitch implements HasCssName {
  X_LOW {
    @Override
    public String getCssName() {
      return "x-low";
    }
  },
  LOW {
    @Override
    public String getCssName() {
      return "low";
    }
  },
  MEDIUM {
    @Override
    public String getCssName() {
      return "medium";
    }
  },
  HIGH {
    @Override
    public String getCssName() {
      return "high";
    }
  },
  X_HIGH {
    @Override
    public String getCssName() {
      return "x-high";
    }
  },
  INHERIT {
    @Override
    public String getCssName() {
      return "inherit";
    }
  }
}
