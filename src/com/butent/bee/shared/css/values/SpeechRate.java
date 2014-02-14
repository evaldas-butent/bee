package com.butent.bee.shared.css.values;

import com.butent.bee.shared.css.HasCssName;

public enum SpeechRate implements HasCssName {
  X_SLOW {
    @Override
    public String getCssName() {
      return "x-slow";
    }
  },
  SLOW {
    @Override
    public String getCssName() {
      return "slow";
    }
  },
  MEDIUM {
    @Override
    public String getCssName() {
      return "medium";
    }
  },
  FAST {
    @Override
    public String getCssName() {
      return "fast";
    }
  },
  X_FAST {
    @Override
    public String getCssName() {
      return "x-fast";
    }
  },
  FASTER {
    @Override
    public String getCssName() {
      return "faster";
    }
  },
  SLOWER {
    @Override
    public String getCssName() {
      return "slower";
    }
  },
  INHERIT {
    @Override
    public String getCssName() {
      return "inherit";
    }
  }
}
