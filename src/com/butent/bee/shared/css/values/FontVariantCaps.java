package com.butent.bee.shared.css.values;

import com.butent.bee.shared.css.HasCssName;

public enum FontVariantCaps implements HasCssName {
  NORMAL {
    @Override
    public String getCssName() {
      return "normal";
    }
  },
  SMALL_CAPS {
    @Override
    public String getCssName() {
      return "small-caps";
    }
  },
  ALL_SMALL_CAPS {
    @Override
    public String getCssName() {
      return "all-small-caps";
    }
  },
  PETITE_CAPS {
    @Override
    public String getCssName() {
      return "petite-caps";
    }
  },
  ALL_PETITE_CAPS {
    @Override
    public String getCssName() {
      return "all-petite-caps";
    }
  },
  UNICASE {
    @Override
    public String getCssName() {
      return "unicase";
    }
  },
  TITLING_CAPS {
    @Override
    public String getCssName() {
      return "titling-caps";
    }
  }
}
