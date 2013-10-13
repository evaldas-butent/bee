package com.butent.bee.shared.css.values;

import com.google.gwt.dom.client.Style.HasCssName;

public enum DropInitialBeforeAdjust implements HasCssName {
  BEFORE_EDGE {
    @Override
    public String getCssName() {
      return "before-edge";
    }
  },
  TEXT_BEFORE_EDGE {
    @Override
    public String getCssName() {
      return "text-before-edge";
    }
  },
  MIDDLE {
    @Override
    public String getCssName() {
      return "middle";
    }
  },
  CENTRAL {
    @Override
    public String getCssName() {
      return "central";
    }
  },
  AFTER_EDGE {
    @Override
    public String getCssName() {
      return "after-edge";
    }
  },
  TEXT_AFTER_EDGE {
    @Override
    public String getCssName() {
      return "text-after-edge";
    }
  },
  IDEOGRAPHIC {
    @Override
    public String getCssName() {
      return "ideographic";
    }
  },
  HANGING {
    @Override
    public String getCssName() {
      return "hanging";
    }
  },
  MATHEMATICAL {
    @Override
    public String getCssName() {
      return "mathematical";
    }
  }
}
