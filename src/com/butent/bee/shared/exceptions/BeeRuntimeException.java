package com.butent.bee.shared.exceptions;

import com.butent.bee.shared.logging.BeeLogger;
import com.butent.bee.shared.logging.LogUtils;

/**
 * The class {@code BeeRuntimeException} logs runtime errors using ERROR message level.
 */
@SuppressWarnings("serial")
public class BeeRuntimeException extends RuntimeException {

  private static final BeeLogger logger = LogUtils.getLogger(BeeRuntimeException.class);

  public BeeRuntimeException() {
    super();
    logger.error(this);
  }

  public BeeRuntimeException(String message) {
    super(message);
    logger.error(this);
  }

  public BeeRuntimeException(String message, Throwable cause) {
    super(message, cause);
    logger.error(cause, message);
  }

  public BeeRuntimeException(Throwable cause) {
    super(cause);
    logger.error(cause);
  }
}
