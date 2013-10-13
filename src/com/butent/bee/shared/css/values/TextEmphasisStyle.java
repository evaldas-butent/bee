package com.butent.bee.shared.css.values;

import com.google.gwt.dom.client.Style.HasCssName;

public enum TextEmphasisStyle implements HasCssName {
  NONE {
    @Override
    public String getCssName() {
      return "none";
    }
  },
  FILLED {
    @Override
    public String getCssName() {
      return "filled";
    }
  },
  OPEN {
    @Override
    public String getCssName() {
      return "open";
    }
  },
  DOT {
    @Override
    public String getCssName() {
      return "dot";
    }
  },
  CIRCLE {
    @Override
    public String getCssName() {
      return "circle";
    }
  },
  DOUBLE_CIRCLE {
    @Override
    public String getCssName() {
      return "double-circle";
    }
  },
  TRIANGLE {
    @Override
    public String getCssName() {
      return "triangle";
    }
  },
  SESAME {
    @Override
    public String getCssName() {
      return "sesame";
    }
  }
}
