package com.butent.bee.egg.shared.exceptions;

public class BeeException extends Exception {
  private static final long serialVersionUID = 1L;

  public BeeException() {
    super();
  }

  public BeeException(String message) {
    super(message);
  }

  public BeeException(String message, Throwable cause) {
    super(message, cause);
  }

  public BeeException(Throwable cause) {
    super(cause);
  }
}
