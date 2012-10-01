package com.butent.bee.shared.logging;

import com.butent.bee.shared.Assert;
import com.butent.bee.shared.utils.ArrayUtils;

/**
 * Contains methods used for logging, changing logging level.
 */
public class LogUtils {

  public enum LogLevel {
    ERROR, WARNING, INFO, DEBUG
  }

  private static BeeLoggerFactory loggerFactory;

  public static BeeLogger getLogger(String name) {
    return new BeeLoggerWrapper(name);
  }

  public static BeeLogger getLogger(Class<?> clazz) {
    Assert.notNull(clazz);
    return getLogger(clazz.getName());
  }

  public static void logError(BeeLogger logger, Throwable err, Object... messages) {
    Assert.notNull(err);

    if (ArrayUtils.length(messages) > 0) {
      logger.error(messages);
    }
    logger.error(err);
    logStack(logger, err);
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
      logger.info(++i, ":", el);
    }
  }

  public static void setLoggerFactory(BeeLoggerFactory loggerFactory) {
    LogUtils.loggerFactory = loggerFactory;
  }

  static BeeLogger createLogger(String name) {
    Assert.notNull(loggerFactory);
    return loggerFactory.getLogger(name);
  }

  private LogUtils() {
  }
}
