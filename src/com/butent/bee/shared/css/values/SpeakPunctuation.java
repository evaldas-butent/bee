package com.butent.bee.shared.css.values;

import com.butent.bee.shared.css.HasCssName;

public enum SpeakPunctuation implements HasCssName {
  CODE {
    @Override
    public String getCssName() {
      return "code";
    }
  },
  NONE {
    @Override
    public String getCssName() {
      return "none";
    }
  },
  INHERIT {
    @Override
    public String getCssName() {
      return "inherit";
    }
  }
}
