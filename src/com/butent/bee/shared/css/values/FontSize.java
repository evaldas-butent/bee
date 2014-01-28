package com.butent.bee.shared.css.values;

import com.butent.bee.shared.css.HasCssName;

public enum FontSize implements HasCssName {
  XX_SMALL {
    @Override
    public String getCssName() {
      return "xx-small";
    }
  },
  X_SMALL {
    @Override
    public String getCssName() {
      return "x-small";
    }
  },
  SMALL {
    @Override
    public String getCssName() {
      return "small";
    }
  },
  MEDIUM {
    @Override
    public String getCssName() {
      return "medium";
    }
  },
  LARGE {
    @Override
    public String getCssName() {
      return "large";
    }
  },
  X_LARGE {
    @Override
    public String getCssName() {
      return "x-large";
    }
  },
  XX_LARGE {
    @Override
    public String getCssName() {
      return "xx-large";
    }
  },
  LARGER {
    @Override
    public String getCssName() {
      return "larger";
    }
  },
  SMALLER {
    @Override
    public String getCssName() {
      return "smaller";
    }
  }
}
