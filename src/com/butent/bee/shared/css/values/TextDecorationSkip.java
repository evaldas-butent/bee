package com.butent.bee.shared.css.values;

import com.google.gwt.dom.client.Style.HasCssName;

public enum TextDecorationSkip implements HasCssName {
  NONE {
    @Override
    public String getCssName() {
      return "none";
    }
  },
  OBJECTS {
    @Override
    public String getCssName() {
      return "objects";
    }
  },
  SPACES {
    @Override
    public String getCssName() {
      return "spaces";
    }
  },
  INK {
    @Override
    public String getCssName() {
      return "ink";
    }
  },
  EDGES {
    @Override
    public String getCssName() {
      return "edges";
    }
  },
  BOX_DECORATION {
    @Override
    public String getCssName() {
      return "box-decoration";
    }
  }
}
