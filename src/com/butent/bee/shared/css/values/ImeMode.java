package com.butent.bee.shared.css.values;

import com.google.gwt.dom.client.Style.HasCssName;

public enum ImeMode implements HasCssName {
  AUTO {
    @Override
    public String getCssName() {
      return "auto";
    }
  },
  NORMAL {
    @Override
    public String getCssName() {
      return "normal";
    }
  },
  ACTIVE {
    @Override
    public String getCssName() {
      return "active";
    }
  },
  INACTIVE {
    @Override
    public String getCssName() {
      return "inactive";
    }
  },
  DISABLED {
    @Override
    public String getCssName() {
      return "disabled";
    }
  },
  INHERIT {
    @Override
    public String getCssName() {
      return "inherit";
    }
  }
}
