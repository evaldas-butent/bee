package com.butent.bee.shared.css.values;

import com.google.gwt.dom.client.Style.HasCssName;

public enum OverflowStyle implements HasCssName {
  AUTO {
    @Override
    public String getCssName() {
      return "auto";
    }
  },
  SCROLLBAR {
    @Override
    public String getCssName() {
      return "scrollbar";
    }
  },
  PANNER {
    @Override
    public String getCssName() {
      return "panner";
    }
  },
  MOVE {
    @Override
    public String getCssName() {
      return "move";
    }
  },
  MARQUEE {
    @Override
    public String getCssName() {
      return "marquee";
    }
  },
  MARQUEE_LINE {
    @Override
    public String getCssName() {
      return "marquee-line";
    }
  },
  MARQUEE_BLOCK {
    @Override
    public String getCssName() {
      return "marquee-block";
    }
  }
}
