package com.butent.bee.shared.css.values;

import com.google.gwt.dom.client.Style.HasCssName;

public enum LineStackingStrategy implements HasCssName {
  INLINE_LINE_HEIGHT {
    @Override
    public String getCssName() {
      return "inline-line-height";
    }
  },
  BLOCK_LINE_HEIGHT {
    @Override
    public String getCssName() {
      return "block-line-height";
    }
  },
  MAX_HEIGHT {
    @Override
    public String getCssName() {
      return "max-height";
    }
  },
  GRID_HEIGHT {
    @Override
    public String getCssName() {
      return "grid-height";
    }
  }
}
