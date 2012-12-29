package com.butent.bee.client.validation;

public enum EditorValidation {
  NONE, INPUT, NEW_VALUE, ALL;
  
  public boolean validateInput() {
    return INPUT.equals(this) || ALL.equals(this);
  }

  public boolean validateNewValue() {
    return NEW_VALUE.equals(this) || ALL.equals(this);
  }
}
