package com.butent.bee.shared.css.values;

import com.google.gwt.dom.client.Style.HasCssName;

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
