package com.butent.bee.shared.css.values;

import com.google.gwt.dom.client.Style.HasCssName;

public enum TargetName implements HasCssName {
  CURRENT {
    @Override
    public String getCssName() {
      return "current";
    }
  },
  ROOT {
    @Override
    public String getCssName() {
      return "root";
    }
  },
  PARENT {
    @Override
    public String getCssName() {
      return "parent";
    }
  },
  NEW {
    @Override
    public String getCssName() {
      return "new";
    }
  },
  MODAL {
    @Override
    public String getCssName() {
      return "modal";
    }
  }
}
