package com.butent.bee.shared.css.values;

import com.google.gwt.dom.client.Style.HasCssName;

public enum LineStacking implements HasCssName {
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
  },
  EXCLUDE_RUBY {
    @Override
    public String getCssName() {
      return "exclude-ruby";
    }
  },
  INCLUDE_RUBY {
    @Override
    public String getCssName() {
      return "include-ruby";
    }
  },
  CONSIDER_SHIFTS {
    @Override
    public String getCssName() {
      return "consider-shifts";
    }
  },
  DISREGARD_SHIFTS {
    @Override
    public String getCssName() {
      return "disregard-shifts";
    }
  }
}
