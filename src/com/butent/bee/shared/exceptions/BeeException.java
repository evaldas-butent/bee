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

  public static BeeException error(Throwable err) {
    Throwable cause = err;

    while (cause.getCause() != null) {
      cause = cause.getCause();
    }
    if (cause instanceof BeeException) {
      return (BeeException) cause;
    }
    return new BeeException(cause.getLocalizedMessage(), cause);
  }
}
