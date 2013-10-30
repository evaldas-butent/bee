package com.butent.bee.shared.css.values;

import com.google.gwt.dom.client.Style.HasCssName;

public enum RenderingIntent implements HasCssName {
  AUTO {
    @Override
    public String getCssName() {
      return "auto";
    }
  },
  PERCEPTUAL {
    @Override
    public String getCssName() {
      return "perceptual";
    }
  },
  RELATIVE_COLORIMETRIC {
    @Override
    public String getCssName() {
      return "relative-colorimetric";
    }
  },
  SATURATION {
    @Override
    public String getCssName() {
      return "saturation";
    }
  },
  ABSOLUTE_COLORIMETRIC {
    @Override
    public String getCssName() {
      return "absolute-colorimetric";
    }
  },
  INHERIT {
    @Override
    public String getCssName() {
      return "inherit";
    }
  }
}
