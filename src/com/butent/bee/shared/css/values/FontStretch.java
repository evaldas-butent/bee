package com.butent.bee.shared.css.values;

import com.butent.bee.shared.css.HasCssName;

public enum FontStretch implements HasCssName {
  NORMAL {
    @Override
    public String getCssName() {
      return "normal";
    }
  },
  ULTRA_CONDENSED {
    @Override
    public String getCssName() {
      return "ultra-condensed";
    }
  },
  EXTRA_CONDENSED {
    @Override
    public String getCssName() {
      return "extra-condensed";
    }
  },
  CONDENSED {
    @Override
    public String getCssName() {
      return "condensed";
    }
  },
  SEMI_CONDENSED {
    @Override
    public String getCssName() {
      return "semi-condensed";
    }
  },
  SEMI_EXPANDED {
    @Override
    public String getCssName() {
      return "semi-expanded";
    }
  },
  EXPANDED {
    @Override
    public String getCssName() {
      return "expanded";
    }
  },
  EXTRA_EXPANDED {
    @Override
    public String getCssName() {
      return "extra-expanded";
    }
  },
  ULTRA_EXPANDED {
    @Override
    public String getCssName() {
      return "ultra-expanded";
    }
  }
}
