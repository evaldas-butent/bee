package com.butent.bee.shared.css.values;

import com.google.gwt.dom.client.Style.HasCssName;

public enum PageBreakAfter implements HasCssName {
  AUTO {
    @Override
    public String getCssName() {
      return "auto";
    }
  },
  ALWAYS {
    @Override
    public String getCssName() {
      return "always";
    }
  },
  AVOID {
    @Override
    public String getCssName() {
      return "avoid";
    }
  },
  LEFT {
    @Override
    public String getCssName() {
      return "left";
    }
  },
  RIGHT {
    @Override
    public String getCssName() {
      return "right";
    }
  },
  INHERIT {
    @Override
    public String getCssName() {
      return "inherit";
    }
  }
}
