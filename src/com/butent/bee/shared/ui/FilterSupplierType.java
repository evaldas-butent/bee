package com.butent.bee.shared.ui;

import com.butent.bee.shared.utils.BeeUtils;

public enum FilterSupplierType {
  VALUE("value"),
  LIST("list"),
  ENUM("enum"),
  ID("id");

  public static FilterSupplierType getByTypeCode(String code) {
    if (code == null || code.isEmpty()) {
      return null;
    }
    for (FilterSupplierType type : FilterSupplierType.values()) {
      if (BeeUtils.same(type.getTypeCode(), code)) {
        return type;
      }
    }
    return null;
  }

  private final String typeCode;
  
  private FilterSupplierType(String typeCode) {
    this.typeCode = typeCode;
  }

  public String getTypeCode() {
    return typeCode;
  }
}
