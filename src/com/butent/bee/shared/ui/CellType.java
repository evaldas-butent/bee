package com.butent.bee.shared.ui;

import com.google.common.base.Strings;
import com.google.gwt.safehtml.shared.SafeHtml;
import com.google.gwt.safehtml.shared.SafeHtmlUtils;

import com.butent.bee.shared.utils.BeeUtils;

/**
 * Contains a list of possible cell types.
 */

public enum CellType {
  HTML("html") {
    @Override
    public SafeHtml renderSafeHtml(String input) {
      return SafeHtmlUtils.fromTrustedString(Strings.nullToEmpty(input));
    }
  },

  TEXT("text") {
    @Override
    public SafeHtml renderSafeHtml(String input) {
      return SafeHtmlUtils.fromString(Strings.nullToEmpty(input));
    }
  };

  public static CellType getByCode(String code) {
    if (!BeeUtils.isEmpty(code)) {
      for (CellType type : CellType.values()) {
        if (BeeUtils.same(type.getCode(), code)) {
          return type;
        }
      }
    }
    return null;
  }

  private final String code;

  CellType(String code) {
    this.code = code;
  }

  public String getCode() {
    return code;
  }

  public abstract SafeHtml renderSafeHtml(String input);
}
