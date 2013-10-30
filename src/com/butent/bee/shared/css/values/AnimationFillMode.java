package com.butent.bee.shared.css.values;

import com.google.gwt.dom.client.Style.HasCssName;

public enum AnimationFillMode implements HasCssName {
  NONE {
    @Override
    public String getCssName() {
      return "none";
    }
  },
  FORWARDS {
    @Override
    public String getCssName() {
      return "forwards";
    }
  },
  BACKWARDS {
    @Override
    public String getCssName() {
      return "backwards";
    }
  },
  BOTH {
    @Override
    public String getCssName() {
      return "both";
    }
  }
}
