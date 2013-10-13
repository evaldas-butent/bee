package com.butent.bee.shared.css.values;

import com.google.gwt.dom.client.Style.HasCssName;

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
