package com.butent.bee.shared.ui;

import com.butent.bee.shared.utils.BeeUtils;

public enum CssUnit implements HasCaption {
  PX("px"),
  PCT("%"),
  EM("em"),
  EX("ex"),
  PT("pt"),
  PC("pc"),
  IN("in"),
  CM("cm"),
  MM("mm");
  
  public static CssUnit parse(String input) {
    if (BeeUtils.isEmpty(input)) {
      return null;
    }

    for (CssUnit unit : CssUnit.values()) {
      if (BeeUtils.same(unit.getCaption(), input)) {
        return unit;
      }
    }
    return null;
  }
  
  public static CssUnit parse(String input, CssUnit defUnit) {
    return BeeUtils.nvl(parse(input), defUnit);
  }
  
  private final String caption;

  private CssUnit(String caption) {
    this.caption = caption;
  }

  @Override
  public String getCaption() {
    return caption;
  }
}
