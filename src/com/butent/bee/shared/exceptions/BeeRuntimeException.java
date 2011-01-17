package com.butent.bee.shared.exceptions;

import com.butent.bee.shared.utils.LogUtils;

@SuppressWarnings("serial")
public class BeeRuntimeException extends RuntimeException {

  public BeeRuntimeException() {
    super();
    LogUtils.error(this);
  }

  public BeeRuntimeException(String message) {
    super(message);
    LogUtils.error(this, message);
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
