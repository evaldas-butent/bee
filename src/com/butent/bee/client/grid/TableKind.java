package com.butent.bee.client.grid;

import com.butent.bee.shared.utils.BeeUtils;

public enum TableKind {
  CONTROLS("controls", "controls"),
  OUTPUT("output", "output"),
  CUSTOM("custom", null);

  public static TableKind parse(String code) {
    for (TableKind kind : TableKind.values()) {
      if (BeeUtils.same(kind.getCode(), code)) {
        return kind;
      }
    }
    return null;
  }

  private final String code;
  private final String styleSuffix;

  TableKind(String code, String styleSuffix) {
    this.code = code;
    this.styleSuffix = styleSuffix;
  }

  public String getCode() {
    return code;
  }

  public String getStyleSuffix() {
    return styleSuffix;
  }
}
