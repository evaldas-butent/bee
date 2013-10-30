package com.butent.bee.shared.css.values;

import com.google.gwt.dom.client.Style.HasCssName;

public enum Display implements HasCssName {
  INLINE {
    @Override
    public String getCssName() {
      return "inline";
    }
  },
  BLOCK {
    @Override
    public String getCssName() {
      return "block";
    }
  },
  INLINE_BLOCK {
    @Override
    public String getCssName() {
      return "inline-block";
    }
  },
  LIST_ITEM {
    @Override
    public String getCssName() {
      return "list-item";
    }
  },
  RUN_IN {
    @Override
    public String getCssName() {
      return "run-in";
    }
  },
  COMPACT {
    @Override
    public String getCssName() {
      return "compact";
    }
  },
  TABLE {
    @Override
    public String getCssName() {
      return "table";
    }
  },
  INLINE_TABLE {
    @Override
    public String getCssName() {
      return "inline-table";
    }
  },
  TABLE_ROW_GROUP {
    @Override
    public String getCssName() {
      return "table-row-group";
    }
  },
  TABLE_HEADER_GROUP {
    @Override
    public String getCssName() {
      return "table-header-group";
    }
  },
  TABLE_FOOTER_GROUP {
    @Override
    public String getCssName() {
      return "table-footer-group";
    }
  },
  TABLE_ROW {
    @Override
    public String getCssName() {
      return "table-row";
    }
  },
  TABLE_COLUMN_GROUP {
    @Override
    public String getCssName() {
      return "table-column-group";
    }
  },
  TABLE_COLUMN {
    @Override
    public String getCssName() {
      return "table-column";
    }
  },
  TABLE_CELL {
    @Override
    public String getCssName() {
      return "table-cell";
    }
  },
  TABLE_CAPTION {
    @Override
    public String getCssName() {
      return "table-caption";
    }
  },
  RUBY {
    @Override
    public String getCssName() {
      return "ruby";
    }
  },
  RUBY_BASE {
    @Override
    public String getCssName() {
      return "ruby-base";
    }
  },
  RUBY_TEXT {
    @Override
    public String getCssName() {
      return "ruby-text";
    }
  },
  RUBY_BASE_GROUP {
    @Override
    public String getCssName() {
      return "ruby-base-group";
    }
  },
  RUBY_TEXT_GROUP {
    @Override
    public String getCssName() {
      return "ruby-text-group";
    }
  },
  FLEX {
    @Override
    public String getCssName() {
      return "flex";
    }
  },
  INLINE_FLEX {
    @Override
    public String getCssName() {
      return "inline-flex";
    }
  },
  GRID {
    @Override
    public String getCssName() {
      return "grid";
    }
  },
  INLINE_GRID {
    @Override
    public String getCssName() {
      return "inline-grid";
    }
  },
  MARKER {
    @Override
    public String getCssName() {
      return "marker";
    }
  },
  NORMAL {
    @Override
    public String getCssName() {
      return "normal";
    }
  },
  NONE {
    @Override
    public String getCssName() {
      return "none";
    }
  }
}
