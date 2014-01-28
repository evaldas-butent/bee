package com.butent.bee.shared.css.values;

import com.butent.bee.shared.css.HasCssName;

public enum AnimationFillMode implements HasCssName {
  NONE {
    @Override
    public String getCssName() {
      return "none";
    }
  },
  FORWARDS {
    @Override
    public String getCssName() {
      return "forwards";
    }
  },
  BACKWARDS {
    @Override
    public String getCssName() {
      return "backwards";
    }
  },
  BOTH {
    @Override
    public String getCssName() {
      return "both";
    }
  }
}
