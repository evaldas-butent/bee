package com.butent.bee.shared.css.values;

import com.google.gwt.dom.client.Style.HasCssName;

public enum BorderStyle implements HasCssName {
  NONE {
    @Override
    public String getCssName() {
      return "none";
    }
  },
  HIDDEN {
    @Override
    public String getCssName() {
      return "hidden";
    }
  },
  DOTTED {
    @Override
    public String getCssName() {
      return "dotted";
    }
  },
  DASHED {
    @Override
    public String getCssName() {
      return "dashed";
    }
  },
  SOLID {
    @Override
    public String getCssName() {
      return "solid";
    }
  },
  DOUBLE {
    @Override
    public String getCssName() {
      return "double";
    }
  },
  GROOVE {
    @Override
    public String getCssName() {
      return "groove";
    }
  },
  RIDGE {
    @Override
    public String getCssName() {
      return "ridge";
    }
  },
  INSET {
    @Override
    public String getCssName() {
      return "inset";
    }
  },
  OUTSET {
    @Override
    public String getCssName() {
      return "outset";
    }
  }
}
