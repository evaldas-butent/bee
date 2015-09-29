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

  private BeeException(Throwable cause) {
    super(cause);
  }

  public static BeeException error(Throwable err) {
    Throwable cause = err;

    while (cause.getCause() != null) {
      cause = cause.getCause();
    }
    return new BeeException(cause);
  }
}
