package com.butent.bee.shared.css.values;

import com.butent.bee.shared.css.HasCssName;

public enum FontWeight implements HasCssName {
  NORMAL {
    @Override
    public String getCssName() {
      return "normal";
    }
  },
  BOLD {
    @Override
    public String getCssName() {
      return "bold";
    }
  },
  BOLDER {
    @Override
    public String getCssName() {
      return "bolder";
    }
  },
  LIGHTER {
    @Override
    public String getCssName() {
      return "lighter";
    }
  },
  _100 {
    @Override
    public String getCssName() {
      return "100";
    }
  },
  _200 {
    @Override
    public String getCssName() {
      return "200";
    }
  },
  _300 {
    @Override
    public String getCssName() {
      return "300";
    }
  },
  _400 {
    @Override
    public String getCssName() {
      return "400";
    }
  },
  _500 {
    @Override
    public String getCssName() {
      return "500";
    }
  },
  _600 {
    @Override
    public String getCssName() {
      return "600";
    }
  },
  _700 {
    @Override
    public String getCssName() {
      return "700";
    }
  },
  _800 {
    @Override
    public String getCssName() {
      return "800";
    }
  },
  _900 {
    @Override
    public String getCssName() {
      return "900";
    }
  }
}
