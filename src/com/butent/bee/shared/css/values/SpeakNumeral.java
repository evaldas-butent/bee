package com.butent.bee.shared.css.values;

import com.butent.bee.shared.css.HasCssName;

public enum SpeakNumeral implements HasCssName {
  DIGITS {
    @Override
    public String getCssName() {
      return "digits";
    }
  },
  CONTINUOUS {
    @Override
    public String getCssName() {
      return "continuous";
    }
  },
  INHERIT {
    @Override
    public String getCssName() {
      return "inherit";
    }
  }
}
