package com.butent.bee.shared.css.values;

import com.google.gwt.dom.client.Style.HasCssName;

public enum Volume implements HasCssName {
  SILENT {
    @Override
    public String getCssName() {
      return "silent";
    }
  },
  X_SOFT {
    @Override
    public String getCssName() {
      return "x-soft";
    }
  },
  SOFT {
    @Override
    public String getCssName() {
      return "soft";
    }
  },
  MEDIUM {
    @Override
    public String getCssName() {
      return "medium";
    }
  },
  LOUD {
    @Override
    public String getCssName() {
      return "loud";
    }
  },
  X_LOUD {
    @Override
    public String getCssName() {
      return "x-loud";
    }
  },
  INHERIT {
    @Override
    public String getCssName() {
      return "inherit";
    }
  }
}
