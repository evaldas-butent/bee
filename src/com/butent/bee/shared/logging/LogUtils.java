package com.butent.bee.shared.logging;

import com.butent.bee.shared.Assert;
import com.butent.bee.shared.BeeConst;
import com.butent.bee.shared.time.TimeUtils;
import com.butent.bee.shared.utils.BeeUtils;

/**
 * Contains methods used for logging, changing logging level.
 */
public final class LogUtils {

  private static BeeLoggerFactory loggerFactory;

  public static BeeLogger getLogger(String name) {
    Assert.notNull(name);

    if (BeeConst.isClient()) {
      return createLogger(name);
    }
    return new BeeLoggerWrapper(name);
  }

  public static BeeLogger getLogger(Class<?> clazz) {
    Assert.notNull(clazz);
    return getLogger(clazz.getName());
  }

  public static BeeLogger getRootLogger() {
    if (loggerFactory != null) {
      return loggerFactory.getRootLogger();
    }
    return null;
  }

  public static void logStack(BeeLogger logger, LogLevel level, Throwable err) {
    Assert.notNull(logger);
    Assert.notNull(level);
    Assert.notNull(err);

    int i = 0;
    for (StackTraceElement el : err.getStackTrace()) {
      logger.log(level, BeeUtils.bracket(++i), el);
    }
  }

  public static long profile(BeeLogger logger, String caption, long start) {
    logger.debug(caption, TimeUtils.elapsedMillis(start));
    return System.currentTimeMillis();
  }

  public static void setLoggerFactory(BeeLoggerFactory loggerFactory) {
    LogUtils.loggerFactory = loggerFactory;
  }

  public static void stopLogger() {
    if (LogUtils.loggerFactory != null) {
      LogUtils.loggerFactory.stop();
    }
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
