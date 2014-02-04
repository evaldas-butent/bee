package com.butent.bee.shared.css.values;

import com.butent.bee.shared.css.HasCssName;

public enum TextEmphasisPosition implements HasCssName {
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
