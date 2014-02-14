package com.butent.bee.shared.css.values;

import com.butent.bee.shared.css.HasCssName;

public enum DominantBaseline implements HasCssName {
  AUTO {
    @Override
    public String getCssName() {
      return "auto";
    }
  },
  USE_SCRIPT {
    @Override
    public String getCssName() {
      return "use-script";
    }
  },
  NO_CHANGE {
    @Override
    public String getCssName() {
      return "no-change";
    }
  },
  RESET_SIZE {
    @Override
    public String getCssName() {
      return "reset-size";
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
  IDEOGRAPHIC {
    @Override
    public String getCssName() {
      return "ideographic";
    }
  },
  MATHEMATICAL {
    @Override
    public String getCssName() {
      return "mathematical";
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
  TEXT_AFTER_EDGE {
    @Override
    public String getCssName() {
      return "text-after-edge";
    }
  },
  TEXT_BEFORE_EDGE {
    @Override
    public String getCssName() {
      return "text-before-edge";
    }
  }
}
