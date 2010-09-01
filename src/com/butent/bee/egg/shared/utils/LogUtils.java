package com.butent.bee.egg.shared.utils;

import java.util.logging.Logger;

import com.butent.bee.egg.shared.Assert;
import com.butent.bee.egg.shared.BeeDate;

public class LogUtils {
  public static String now() {
    return new BeeDate().toLog();
  }

  public static String dateToLog(long millis) {
    return new BeeDate(millis).toLog();
  }

  public static String dateToLog(BeeDate dt) {
    Assert.notNull(dt);
    return dt.toLog();
  }

  public static void info(Logger logger, String msg) {
    Assert.notNull(logger);
    logger.info(msg);
  }

  public static void info(Logger logger, Object... obj) {
    Assert.notNull(logger);
    Assert.parameterCount(obj.length + 1, 2);
    logger.info(BeeUtils.concat(1, obj));
  }

  public static void infoNow(Logger logger, Object... obj) {
    Assert.notNull(logger);
    logger.info(BeeUtils.concat(1, now(), obj));
  }

  public static void error(Logger logger, Throwable err, Object... obj) {
    if (err != null)
      logger.severe(transformError(err));
    if (obj.length > 0)
      logger.severe(BeeUtils.concat(1, obj));

    if (err != null) {
      stack(logger, err);
    }
  }

  public static void stack(Logger logger, Throwable err) {
    StackTraceElement[] arr = err.getStackTrace();
    for (int i = 0; i < arr.length; i++)
      logger.info("[" + i + "] " + arr[i].toString());
  }

  public static void warning(Logger logger, String msg) {
    Assert.notNull(logger);
    logger.warning(msg);
  }

  public static void warning(Logger logger, Object... obj) {
    Assert.notNull(logger);
    Assert.parameterCount(obj.length + 1, 2);
    logger.warning(BeeUtils.concat(1, obj));
  }

  public static void warning(Logger logger, Throwable err, Object... obj) {
    if (err != null)
      logger.warning(transformError(err));
    if (obj.length > 0)
      logger.warning(BeeUtils.concat(1, obj));
  }

  private static String transformError(Throwable err) {
    return err.toString();
  }

}
