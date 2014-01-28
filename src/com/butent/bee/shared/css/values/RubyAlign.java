package com.butent.bee.shared.css.values;

import com.butent.bee.shared.css.HasCssName;

public enum RubyAlign implements HasCssName {
  START {
    @Override
    public String getCssName() {
      return "start";
    }
  },
  CENTER {
    @Override
    public String getCssName() {
      return "center";
    }
  },
  SPACE_BETWEEN {
    @Override
    public String getCssName() {
      return "space-between";
    }
  },
  SPACE_AROUND {
    @Override
    public String getCssName() {
      return "space-around";
    }
  }
}
