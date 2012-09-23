package com.butent.bee.shared.ui;

public enum Orientation implements HasCaption {
  HORIZONTAL, VERTICAL;

  @Override
  public String getCaption() {
    return name().toLowerCase();
  }
}
