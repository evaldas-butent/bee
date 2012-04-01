package com.butent.bee.shared.ui;

import com.butent.bee.shared.utils.BeeUtils;

public enum RendererType {
  LIST("list"),
  MAP("map"),
  RANGE("range"),
  ENUM("enum");

  public static RendererType getByTypeCode(String code) {
    if (code == null || code.isEmpty()) {
      return null;
    }
    for (RendererType type : RendererType.values()) {
      if (BeeUtils.same(type.getTypeCode(), code)) {
        return type;
      }
    }
    return null;
  }

  private final String typeCode;
  
  private RendererType(String typeCode) {
    this.typeCode = typeCode;
  }

  public String getTypeCode() {
    return typeCode;
  }
}
