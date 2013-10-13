package com.butent.bee.shared.css.values;

import com.google.gwt.dom.client.Style.HasCssName;

public enum DropInitialBeforeAlign implements HasCssName {
  BASELINE {
    @Override
    public String getCssName() {
      return "baseline";
    }
  },
  USE_SCRIPT {
    @Override
    public String getCssName() {
      return "use-script";
    }
  },
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
  CENTRAL {
    @Override
    public String getCssName() {
      return "central";
    }
  },
  MIDDLE {
    @Override
    public String getCssName() {
      return "middle";
    }
  },
  IDEOGRAPHIC {
    @Override
    public String getCssName() {
      return "ideographic";
    }
  },
  ALPHABETIC {
    @Override
    public String getCssName() {
      return "alphabetic";
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
  },
  CAPS_HEIGHT {
    @Override
    public String getCssName() {
      return "caps-height";
    }
  }
}
