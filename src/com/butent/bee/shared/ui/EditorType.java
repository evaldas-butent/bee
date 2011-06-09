package com.butent.bee.shared.ui;

import com.butent.bee.shared.utils.BeeUtils;

/**
 * Contains a list of possible data editing user interface components.
 */

public enum EditorType {
  LIST("list"),
  PICKER("picker"),
  AREA("area"),
  DATE("date"),
  DATETIME("datetime"),
  TEXT("text"),
  RICH("rich"),
  NUMBER("number"),
  INTEGER("integer"),
  SLIDER("slider"),
  SPINNER("spinner"),
  LONG("long"),
  SUGGEST("suggest"),
  TOGGLE("toggle");

  public static EditorType getByTypeCode(String code) {
    if (code == null || code.isEmpty()) {
      return null;
    }
    for (EditorType type : EditorType.values()) {
      if (BeeUtils.same(type.getTypeCode(), code)) {
        return type;
      }
    }
    return null;
  }

  private final String typeCode;

  private EditorType(String typeCode) {
    this.typeCode = typeCode;
  }

  public String getTypeCode() {
    return typeCode;
  }
}
