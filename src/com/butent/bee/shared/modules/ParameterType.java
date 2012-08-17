package com.butent.bee.shared.modules;

import com.butent.bee.shared.ui.HasCaption;

public enum ParameterType implements HasCaption {
  TEXT, NUMBER, BOOLEAN,
  DATE, TIME, DATETIME,
  MAP, SET, LIST;

  @Override
  public String getCaption() {
    return name();
  }
}
