package com.butent.bee.shared.css.values;

import com.butent.bee.shared.css.HasCssName;

public enum DropInitialAfterAdjust implements HasCssName {
  CENTRAL {
    @Override
    public String getCssName() {
      return "central";
    }
  },
  MIDDLE {
    @Override
    public String getCssName() {
      return "middle";
    }
  },
  AFTER_EDGE {
    @Override
    public String getCssName() {
      return "after-edge";
    }
  },
  TEXT_AFTER_EDGE {
    @Override
    public String getCssName() {
      return "text-after-edge";
    }
  },
  IDEOGRAPHIC {
    @Override
    public String getCssName() {
      return "ideographic";
    }
  },
  ALPHABETIC {
    @Override
    public String getCssName() {
      return "alphabetic";
    }
  },
  MATHEMATICAL {
    @Override
    public String getCssName() {
      return "mathematical";
    }
  }
}
