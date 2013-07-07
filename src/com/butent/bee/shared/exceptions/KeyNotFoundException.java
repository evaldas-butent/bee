package com.butent.bee.shared.exceptions;

/**
 * Extends {@code BeeRuntimeException} class and is thrown when a specified key is not found.
 */

@SuppressWarnings("serial")
public class KeyNotFoundException extends BeeRuntimeException {

  private final Object key;

  public KeyNotFoundException(Object key) {
    super();
    this.key = key;
  }

  public Object getKey() {
    return key;
  }
}
