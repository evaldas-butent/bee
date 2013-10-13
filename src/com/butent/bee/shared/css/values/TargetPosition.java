package com.butent.bee.shared.css.values;

import com.google.gwt.dom.client.Style.HasCssName;

public enum TargetPosition implements HasCssName {
  ABOVE {
    @Override
    public String getCssName() {
      return "above";
    }
  },
  BEHIND {
    @Override
    public String getCssName() {
      return "behind";
    }
  },
  FRONT {
    @Override
    public String getCssName() {
      return "front";
    }
  },
  BACK {
    @Override
    public String getCssName() {
      return "back";
    }
  }
}
