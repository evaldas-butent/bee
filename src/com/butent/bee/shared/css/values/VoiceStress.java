package com.butent.bee.shared.css.values;

import com.google.gwt.dom.client.Style.HasCssName;

public enum VoiceStress implements HasCssName {
  NORMAL {
    @Override
    public String getCssName() {
      return "normal";
    }
  },
  STRONG {
    @Override
    public String getCssName() {
      return "strong";
    }
  },
  MODERATE {
    @Override
    public String getCssName() {
      return "moderate";
    }
  },
  NONE {
    @Override
    public String getCssName() {
      return "none";
    }
  },
  REDUCED {
    @Override
    public String getCssName() {
      return "reduced";
    }
  }
}
