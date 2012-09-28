package com.butent.bee.shared.logging;

import com.butent.bee.shared.Assert;

/**
 * Contains methods used for logging, changing logging level.
 */
public class LogUtils {

  public enum LogLevel {
    ERROR, WARNING, INFO, DEBUG
  }

  private static BeeLoggerFactory loggerFactory;

  public static BeeLogger createLogger(String name) {
    Assert.notNull(loggerFactory);
    return loggerFactory.getLogger(name);
  }

  public static BeeLogger getLogger(String name) {
    return new BeeLoggerWrapper(name);
  }

  public static BeeLogger getLogger(Class<?> clazz) {
    Assert.notNull(clazz);
    return getLogger(clazz.getName());
  }

  public static void setLoggerFactory(BeeLoggerFactory loggerFactory) {
    LogUtils.loggerFactory = loggerFactory;
  }

  /**
   * Log an error stack trace with {@code logger} using INFO message level.
   * 
   * @param logger the Logger to log to
   * @param err the error's stack trace to log
   */
  public static void stack(BeeLogger logger, Throwable err) {
    int i = 0;
    for (StackTraceElement el : err.getStackTrace()) {
      logger.info("[" + ++i + "] " + el.toString());
    }
  }

  private LogUtils() {
  }
}
