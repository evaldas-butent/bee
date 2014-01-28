package com.butent.bee.shared.css.values;

import com.butent.bee.shared.css.HasCssName;

public enum AnimationDirection implements HasCssName {
  NORMAL {
    @Override
    public String getCssName() {
      return "normal";
    }
  },
  REVERSE {
    @Override
    public String getCssName() {
      return "reverse";
    }
  },
  ALTERNATE {
    @Override
    public String getCssName() {
      return "alternate";
    }
  },
  ALTERNATE_REVERSE {
    @Override
    public String getCssName() {
      return "alternate-reverse";
    }
  }
}
