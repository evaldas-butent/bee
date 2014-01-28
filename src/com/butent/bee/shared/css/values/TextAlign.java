package com.butent.bee.shared.css.values;

import com.butent.bee.shared.css.HasCssName;

public enum TextAlign implements HasCssName {
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
  },
  MATCH_PARENT {
    @Override
    public String getCssName() {
      return "match-parent";
    }
  },
  START_END {
    @Override
    public String getCssName() {
      return "start end";
    }
  }
}
