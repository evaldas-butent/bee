package com.butent.bee.egg.shared.utils;

import com.butent.bee.egg.client.BeeKeeper;
import com.butent.bee.egg.shared.Assert;
import com.butent.bee.egg.shared.BeeConst;
import com.butent.bee.egg.shared.BeeDate;

import java.util.logging.Level;
import java.util.logging.Logger;

public class LogUtils {
  private static Logger defaultLogger = null;

  public static String dateToLog(BeeDate dt) {
    Assert.notNull(dt);
    return dt.toLog();
  }

  public static String dateToLog(long millis) {
    return new BeeDate(millis).toLog();
  }

  public static void error(Logger logger, Throwable err, Object... obj) {
    if (err != null) {
      logger.severe(transformError(err));
    }
    if (obj.length > 0) {
      logger.severe(BeeUtils.concat(1, obj));
    }

    if (err != null) {
      stack(logger, err);
    }
  }

  public static void error(Throwable err, Object... obj) {
    error(getDefaultLogger(), err, obj);
  }

  public static Logger getDefaultLogger() {
    if (defaultLogger == null) {
      if (BeeConst.isClient()) {
        setDefaultLogger(BeeKeeper.getLog().getLogger());
      } else {
        setDefaultLogger(Logger.getLogger(LogUtils.class.getName()));
      }
    }

    return defaultLogger;
  }

  public static void info(Logger logger, Object... obj) {
    Assert.notNull(logger);
    Assert.parameterCount(obj.length + 1, 2);
    logger.info(BeeUtils.concat(1, obj));
  }

  public static void info(Logger logger, String msg) {
    Assert.notNull(logger);
    logger.info(msg);
  }

  public static void infoNow(Logger logger, Object... obj) {
    Assert.notNull(logger);
    logger.info(BeeUtils.concat(1, now(), obj));
  }

  public static boolean isOff(Level level) {
    if (level == null) {
      return false;
    } else {
      return level == Level.OFF;
    }
  }

  public static void log(Level level, Object... obj) {
    log(getDefaultLogger(), level, obj);
  }

  public static void log(Logger logger, Level level, Object... obj) {
    Assert.notNull(logger);
    Assert.notNull(level);
    Assert.parameterCount(obj.length + 2, 3);
    logger.log(level, BeeUtils.concat(1, obj));
  }

  public static String now() {
    return new BeeDate().toLog();
  }

  public static void setDefaultLogger(Logger def) {
    defaultLogger = def;
  }

  public static void severe(Logger logger, Object... obj) {
    Assert.notNull(logger);
    Assert.parameterCount(obj.length + 1, 2);
    logger.severe(BeeUtils.concat(1, obj));
  }

  public static void severe(Logger logger, Throwable err, Object... obj) {
    if (err != null) {
      logger.severe(transformError(err));
    }
    if (obj.length > 0) {
      logger.severe(BeeUtils.concat(1, obj));
    }
  }

  public static void severe(Throwable err, Object... obj) {
    severe(getDefaultLogger(), err, obj);
  }

  public static void stack(Logger logger, Throwable err) {
    int i = 0;
    for (StackTraceElement el : err.getStackTrace()) {
      logger.info("[" + ++i + "] " + el.toString());
    }
  }

  public static String transformLevel(Level level) {
    if (level == null) {
      return BeeConst.STRING_EMPTY;
    } else {
      return level.getName();
    }
  }

  public static void warning(Logger logger, Object... obj) {
    Assert.notNull(logger);
    Assert.parameterCount(obj.length + 1, 2);
    logger.warning(BeeUtils.concat(1, obj));
  }

  public static void warning(Logger logger, String msg) {
    Assert.notNull(logger);
    logger.warning(msg);
  }

  public static void warning(Logger logger, Throwable err, Object... obj) {
    if (err != null) {
      logger.warning(transformError(err));
    }
    if (obj.length > 0) {
      logger.warning(BeeUtils.concat(1, obj));
    }
  }

  private static String transformError(Throwable err) {
    return err.toString();
  }
}
