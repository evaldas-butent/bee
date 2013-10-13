package com.butent.bee.shared.css.values;

import com.google.gwt.dom.client.Style.HasCssName;

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
