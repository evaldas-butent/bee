package com.butent.bee.egg.shared.exceptions;

@SuppressWarnings("serial")
public class BeeRuntimeException extends RuntimeException {

  public BeeRuntimeException() {
    super();
  }

  public BeeRuntimeException(String message, Throwable cause) {
    super(message, cause);
  }

  public BeeRuntimeException(String message) {
    super(message);
  }

  public BeeRuntimeException(Throwable cause) {
    super(cause);
  }

}
