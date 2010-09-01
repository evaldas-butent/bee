package com.butent.bee.egg.shared.exceptions;

@SuppressWarnings("serial")
public class KeyNotFoundException extends BeeRuntimeException {
  private Object key = null;

  public KeyNotFoundException() {
    super();
  }

  public KeyNotFoundException(String message, Throwable cause) {
    super(message, cause);
  }

  public KeyNotFoundException(String message) {
    super(message);
  }

  public KeyNotFoundException(Throwable cause) {
    super(cause);
  }

  public KeyNotFoundException(Object key, String message) {
    this(message);
    this.key = key;
  }

  public KeyNotFoundException(Object key) {
    this();
    this.key = key;
  }

  public Object getKey() {
    return key;
  }

  public void setKey(Object key) {
    this.key = key;
  }

}
