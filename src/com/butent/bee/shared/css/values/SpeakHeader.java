package com.butent.bee.shared.css.values;

import com.butent.bee.shared.css.HasCssName;

public enum SpeakHeader implements HasCssName {
  ONCE {
    @Override
    public String getCssName() {
      return "once";
    }
  },
  ALWAYS {
    @Override
    public String getCssName() {
      return "always";
    }
  },
  INHERIT {
    @Override
    public String getCssName() {
      return "inherit";
    }
  }
}
