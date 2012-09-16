package com.butent.bee.shared.exceptions;

@SuppressWarnings("serial")
public class BeeException extends Exception {

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
