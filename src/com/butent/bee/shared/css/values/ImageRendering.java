package com.butent.bee.shared.css.values;

import com.butent.bee.shared.css.HasCssName;

public enum ImageRendering implements HasCssName {
  AUTO {
    @Override
    public String getCssName() {
      return "auto";
    }
  },
  CRISP_EDGES {
    @Override
    public String getCssName() {
      return "crisp-edges";
    }
  },
  PIXELATED {
    @Override
    public String getCssName() {
      return "pixelated";
    }
  }
}
