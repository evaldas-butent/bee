package com.butent.bee.shared.ui;

import com.butent.bee.shared.utils.BeeUtils;

public enum EditorAction {
  REPLACE("replace"),
  SELECT("select"),
  HOME("home"),
  END("end"),
  ADD_FIRST("addFirst"),
  ADD_LAST("addLast");
  
  public static EditorAction getByCode(String code) {
    if (code == null || code.isEmpty()) {
      return null;
    }
    for (EditorAction ea : EditorAction.values()) {
      if (BeeUtils.same(ea.getCode(), code)) {
        return ea;
      }
    }
    return null;
  }
  
  private final String code;
  
  private EditorAction(String code) {
    this.code = code;
  }

  public String getCode() {
    return code;
  }
}
