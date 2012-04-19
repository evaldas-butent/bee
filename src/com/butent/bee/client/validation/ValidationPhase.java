package com.butent.bee.client.validation;

public enum ValidationPhase {
  PRE, DEF, POST;

  public boolean isPostValidation() {
    return POST.equals(this);
  }
  
  public boolean isPreValidation() {
    return PRE.equals(this);
  }
}
