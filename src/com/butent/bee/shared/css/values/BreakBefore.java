package com.butent.bee.shared.css.values;

import com.google.gwt.dom.client.Style.HasCssName;

public enum BreakBefore implements HasCssName {
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
  PAGE {
    @Override
    public String getCssName() {
      return "page";
    }
  },
  COLUMN {
    @Override
    public String getCssName() {
      return "column";
    }
  },
  AVOID_PAGE {
    @Override
    public String getCssName() {
      return "avoid-page";
    }
  },
  AVOID_COLUMN {
    @Override
    public String getCssName() {
      return "avoid-column";
    }
  }
}
