package com.butent.bee.shared.css.values;

import com.google.gwt.dom.client.Style.HasCssName;

public enum TransformStyle implements HasCssName {
  FLAT {
    @Override
    public String getCssName() {
      return "flat";
    }
  },
  PRESERVE_3D {
    @Override
    public String getCssName() {
      return "preserve-3d";
    }
  }
}
