package com.butent.bee.shared.exceptions;

import com.butent.bee.shared.utils.LogUtils;

/**
 * The class {@code BeeRuntimeException} logs runtime errors using SEVERE message level.
 */
@SuppressWarnings("serial")
public class BeeRuntimeException extends RuntimeException {

  public BeeRuntimeException() {
    super();
    LogUtils.error(this);
  }

  public BeeRuntimeException(String message) {
    super(message);
    LogUtils.error(this);
  }

  public BeeRuntimeException(String message, Throwable cause) {
    super(message, cause);
    LogUtils.error(cause, message);
  }

  public BeeRuntimeException(Throwable cause) {
    super(cause);
    LogUtils.error(cause);
  }
}
