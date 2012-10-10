package com.butent.bee.shared.logging;

import com.butent.bee.shared.Assert;
import com.butent.bee.shared.BeeConst;

/**
 * Contains methods used for logging, changing logging level.
 */
public class LogUtils {

  private static BeeLoggerFactory loggerFactory;

  public static BeeLogger getLogger(String name) {
    if (BeeConst.isClient()) {
      return createLogger(name);
    }
    return new BeeLoggerWrapper(name);
  }

  public static BeeLogger getLogger(Class<?> clazz) {
    Assert.notNull(clazz);
    return getLogger(clazz.getName());
  }

  /**
   * Log an error stack trace with {@code logger} using INFO message level.
   * 
   * @param logger the Logger to log to
   * @param err the error's stack trace to log
   */
  public static void logStack(BeeLogger logger, Throwable err) {
    Assert.notNull(err);
    int i = 0;

    for (StackTraceElement el : err.getStackTrace()) {
      logger.debug("[", ++i, "]", el);
    }
  }

  public static void setLoggerFactory(BeeLoggerFactory loggerFactory) {
    LogUtils.loggerFactory = loggerFactory;
  }

  static BeeLogger createLogger(String name) {
    if (loggerFactory != null) {
      return loggerFactory.getLogger(name);
    }
    return null;
  }

  private LogUtils() {
  }
}
