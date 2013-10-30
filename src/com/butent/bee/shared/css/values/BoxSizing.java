package com.butent.bee.shared.css.values;

import com.google.gwt.dom.client.Style.HasCssName;

public enum BoxSizing implements HasCssName {
  CONTENT_BOX {
    @Override
    public String getCssName() {
      return "content-box";
    }
  },
  PADDING_BOX {
    @Override
    public String getCssName() {
      return "padding-box";
    }
  },
  BORDER_BOX {
    @Override
    public String getCssName() {
      return "border-box";
    }
  },
  INHERIT {
    @Override
    public String getCssName() {
      return "inherit";
    }
  }
}
