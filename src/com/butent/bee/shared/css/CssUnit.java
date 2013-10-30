package com.butent.bee.shared.css;

import com.butent.bee.shared.ui.HasCaption;
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
  
  public static final CssUnit DEFAULT = PX;
  
  public static String format(double value, CssUnit unit) {
    return BeeUtils.toString(value) + normalize(unit).getCaption();
  }

  public static String format(int value, CssUnit unit) {
    return BeeUtils.toString(value) + normalize(unit).getCaption();
  }
  
  public static CssUnit normalize(CssUnit unit) {
    return (unit == null) ? DEFAULT : unit;
  }
  
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
