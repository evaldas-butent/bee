package com.butent.bee.shared.css.values;

import com.google.gwt.dom.client.Style.HasCssName;

public enum BackgroundOrigin implements HasCssName {
  BORDER_BOX {
    @Override
    public String getCssName() {
      return "border-box";
    }
  },
  PADDING_BOX {
    @Override
    public String getCssName() {
      return "padding-box";
    }
  },
  CONTENT_BOX {
    @Override
    public String getCssName() {
      return "content-box";
    }
  }
}
