package com.butent.bee.shared.css.values;

import com.butent.bee.shared.css.HasCssName;

public enum AlignmentBaseline implements HasCssName {
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
  }
}
