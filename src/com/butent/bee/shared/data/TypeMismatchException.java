package com.butent.bee.shared.data;

@SuppressWarnings("serial")
public class TypeMismatchException extends DataException {
  public TypeMismatchException(String message) {
    super(Reasons.OTHER, message);
  }
}
