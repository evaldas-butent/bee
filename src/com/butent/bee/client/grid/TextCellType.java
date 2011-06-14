package com.butent.bee.client.grid;

import com.butent.bee.shared.Transformable;

/**
 * Contains a list of available cell types.
 */

public enum TextCellType implements Transformable {
  TEXT(false), TEXT_EDIT(true), TEXT_INPUT(true);

  public static TextCellType get(int idx) {
    if (idx >= 0 && idx < size()) {
      return values()[idx];
    } else {
      return null;
    }
  }

  public static int size() {
    return values().length;
  }

  private boolean editable;

  TextCellType(boolean editable) {
    this.editable = editable;
  }

  public boolean isEditable() {
    return editable;
  }

  public String transform() {
    return name();
  }
}
