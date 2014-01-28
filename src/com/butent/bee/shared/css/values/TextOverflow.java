package com.butent.bee.shared.css.values;

import com.butent.bee.shared.css.HasCssName;

public enum TextOverflow implements HasCssName {
  CLIP {
    @Override
    public String getCssName() {
      return "clip";
    }
  },
  ELLIPSIS {
    @Override
    public String getCssName() {
      return "ellipsis";
    }
  },
  INHERIT {
    @Override
    public String getCssName() {
      return "inherit";
    }
  }
}
