package com.butent.bee.client.validation;

public enum ValidationOrigin {
  CELL, FORM, GRID;

  public boolean isCell() {
    return CELL.equals(this);
  }
  
  public boolean isForm() {
    return FORM.equals(this);
  }

  public boolean isGrid() {
    return GRID.equals(this);
  }
}
