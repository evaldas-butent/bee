package com.butent.bee.shared.utils;

import com.google.common.base.CharMatcher;
import com.google.common.base.Splitter;
import com.google.common.collect.Lists;

import com.butent.bee.client.BeeKeeper;
import com.butent.bee.shared.Assert;
import com.butent.bee.shared.BeeConst;
import com.butent.bee.shared.DateTime;

import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Contains methods used for logging, changing logging level.
 */
public class LogUtils {

  private static final Splitter WORD_SPLITTER =
      Splitter.on(CharMatcher.WHITESPACE).omitEmptyStrings().trimResults();

  private static Logger defaultLogger = null;
  
  private static int maxLineLength = 160; 
  
  /**
   * @param dt the DateTime argument to convert
   * @return a human-readable String representation of DateTime
   */
  public static String dateToLog(DateTime dt) {
    Assert.notNull(dt);
    return dt.toTimeString();
  }

  /**
   * Converts {@code millis} to a readable format.
   * 
   * @param millis the argument to convert
   * @return a String representation of {@code millis} in a human-readable format
   */
  public static String dateToLog(long millis) {
    return new DateTime(millis).toTimeString();
  }

  /**
   * Logs Objects {@code obj} using CONFIG message level.
   * 
   * @param obj the Object to log
   */
  public static void debug(Logger logger, Object... obj) {
    logger.config(BeeUtils.concat(1, obj));
  }

  /**
   * Logs to a specified Logger {@code logger} using a SEVERE message level.
   * 
   * @param logger the Logger to log to
   * @param err the Error message
   * @param obj Objects to log
   */
  public static void error(Logger logger, Throwable err, Object... obj) {
    if (err != null) {
      logger.severe(transformError(err));
    }
    if (obj != null && obj.length > 0) {
      logger.severe(BeeUtils.concat(1, obj));
    }

    if (err != null) {
      stack(logger, err);
    }
  }

  /**
   * Logs to a default Logger using a SEVERE message level.
   * 
   * @param err the Error message
   * @param obj Objects to log
   */
  public static void error(Throwable err, Object... obj) {
    error(getDefaultLogger(), err, obj);
  }

  /**
   * @return the default Logger. If the Logger is {@code null} creates a Logger and returns it.
   */
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

  /**
   * Logs Objects {@code obj} using INFO message level.
   * 
   * @param logger the Logger to log to
   * @param obj Objects to log
   */
  public static void info(Logger logger, Object... obj) {
    Assert.notNull(logger);
    Assert.parameterCount(((obj == null) ? 0 : obj.length) + 1, 2);
    logger.info(BeeUtils.concat(1, obj));
  }

  /**
   * Logs a String message {@code msg} using INFO message level.
   * 
   * @param logger the Logger to log to
   * @param msg message to log
   */
  public static void info(Logger logger, String msg) {
    Assert.notNull(logger);
    logger.info(msg);
  }

  /**
   * Forms a String with current time and Objects {@code obj} and logs it using INFO message level
   * to the specified Logger {@code logger}.
   * 
   * @param logger the Logger to log to
   * @param obj Objects to log
   */
  public static void infoNow(Logger logger, Object... obj) {
    Assert.notNull(logger);
    logger.info(BeeUtils.concat(1, now(), obj));
  }
  
  public static void infoSplit(Logger logger, String head, String msg) {
    infoSplit(logger, head, msg, maxLineLength);
  }

  public static void infoSplit(Logger logger, String head, String msg, int maxLength) {
    split(logger, Level.INFO, head, msg, maxLength);
  }

  /**
   * Forms a String with current time(in UTC) and Objects {@code obj} and logs it using INFO message
   * level to the specified Logger {@code logger}.
   * 
   * @param logger the Logger to log to
   * @param obj Objects to log
   */
  public static void infoUtc(Logger logger, Object... obj) {
    Assert.notNull(logger);
    logger.info(BeeUtils.concat(1, utc(), obj));
  }

  /**
   * Checks if the specified Level {@code level} is equal to Level.OFF.
   * 
   * @param level the argument to check
   * @return true if Levels are equal, otherwise false
   */
  public static boolean isOff(Level level) {
    if (level == null) {
      return false;
    } else {
      return level == Level.OFF;
    }
  }

  /**
   * Logs Objects with a specified message Level {@code level} using a default Logger.
   * 
   * @param level the message level to use
   * @param obj Objects to log
   */
  public static void log(Level level, Object... obj) {
    log(getDefaultLogger(), level, obj);
  }

  /**
   * Logs Objects with a specified message Level {@code level} using a specified Logger
   * {@code logger}.
   * 
   * @param logger the Logger to log to
   * @param level the message level to use
   * @param obj Objects to log
   */
  public static void log(Logger logger, Level level, Object... obj) {
    Assert.notNull(logger);
    Assert.notNull(level);
    Assert.parameterCount(((obj == null) ? 0 : obj.length) + 2, 3);
    logger.log(level, BeeUtils.concat(1, obj));
  }
  
  /**
   * @return the current time in a human-readable format.
   */
  public static String now() {
    return new DateTime().toTimeString();
  }

  /**
   * Sets the default logger to {@code def}.
   * 
   * @param def the value to set default logger to
   */
  public static void setDefaultLogger(Logger def) {
    defaultLogger = def;
  }

  /**
   * Logs Objects to a specified Logger {@code logger} using SEVERE message level.
   * 
   * @param logger the Logger to log to
   * @param obj Objects to log
   */
  public static void severe(Logger logger, Object... obj) {
    Assert.notNull(logger);
    Assert.parameterCount(((obj == null) ? 0 : obj.length) + 1, 2);
    logger.severe(BeeUtils.concat(1, obj));
  }

  /**
   * Logs Objects to a specified Logger {@code logger} using SEVERE message level. Logs {@code err}
   * if it is not {@code null}.
   * 
   * @param logger the Logger to log to
   * @param err the Error to log
   * @param obj Objects to log
   */
  public static void severe(Logger logger, Throwable err, Object... obj) {
    if (err != null) {
      logger.severe(transformError(err));
    }
    if (obj != null && obj.length > 0) {
      logger.severe(BeeUtils.concat(1, obj));
    }
  }

  /**
   * Logs Objects to a default Logger using SEVERE message level. Logs {@code err} if it is not
   * {@code null}.
   * 
   * @param err the Error to log
   * @param obj Objects to log
   */
  public static void severe(Throwable err, Object... obj) {
    severe(getDefaultLogger(), err, obj);
  }

  /**
   * Log an error stack trace with {@code logger} using INFO message level.
   * 
   * @param logger the Logger to log to
   * @param err the error's stack trace to log
   */
  public static void stack(Logger logger, Throwable err) {
    int i = 0;
    for (StackTraceElement el : err.getStackTrace()) {
      logger.info("[" + ++i + "] " + el.toString());
    }
  }

  /**
   * @param level the level to transform.
   * @return the non-localized string name of the Level. If {@code level == null} returns an empty
   *         String.
   */
  public static String transformLevel(Level level) {
    if (level == null) {
      return BeeConst.STRING_EMPTY;
    } else {
      return level.getName();
    }
  }

  /**
   * @return the current time in human-readable format in UTC.
   */
  public static String utc() {
    return new DateTime().toUtcTimeString();
  }

  /**
   * Logs Objects to a specified Logger {@code logger} using WARNING message level.
   * 
   * @param logger the Logger to log to
   * @param obj Objects to log
   */
  public static void warning(Logger logger, Object... obj) {
    Assert.notNull(logger);
    Assert.parameterCount(((obj == null) ? 0 : obj.length) + 1, 2);
    logger.warning(BeeUtils.concat(1, obj));
  }

  /**
   * Logs a String message {@code msg} using WARNING message level.
   * 
   * @param logger the Logger to log to
   * @param msg Objects to log
   */
  public static void warning(Logger logger, String msg) {
    Assert.notNull(logger);
    logger.warning(msg);
  }

  /**
   * Logs Objects to a specified Logger {@code logger} using WARNING message level. Logs {@code err}
   * if it is not {@code null}.
   * 
   * @param logger the Logger to log to
   * @param err the Error to log
   * @param obj the Object to log
   */
  public static void warning(Logger logger, Throwable err, Object... obj) {
    if (err != null) {
      logger.warning(transformError(err));
    }
    if (obj != null && obj.length > 0) {
      logger.warning(BeeUtils.concat(1, obj));
    }
  }

  private static void split(Logger logger, Level level, String head, String msg, int maxLength) {
    Assert.notNull(logger);
    if (BeeUtils.isEmpty(msg)) {
      return;
    }
    
    boolean hasHead = !BeeUtils.isEmpty(msg);
    if (maxLength <= 0 || msg.trim().length() <= maxLength) {
      if (hasHead) {
        logger.log(level, BeeUtils.concat(1, head, msg));
      } else {
        logger.log(level, msg.trim());
      }
      return;
    }
    
    List<String> lst = Lists.newArrayList();
    StringBuilder sb = new StringBuilder();
    
    for (String word : WORD_SPLITTER.split(msg)) {
      if (sb.length() + word.length() >= maxLength) {
        if (sb.length() > 0) {
          lst.add(sb.toString());
          sb.setLength(0);
        }
      } else {
        sb.append(BeeConst.CHAR_SPACE);
      }
      sb.append(word);
    }
    if (sb.length() > 0) {
      lst.add(sb.toString());
    }
    if (lst.isEmpty()) {
      return;
    }
    
    if (lst.size() == 1) {
      logger.log(level, BeeUtils.concat(1, head, lst.get(0)));
      return;
    }
    
    for (int i = 0; i < lst.size(); i++) {
      logger.log(level, BeeUtils.concat(1, head, BeeUtils.progress(i + 1, lst.size()), lst.get(i)));
    }
  }

  private static String transformError(Throwable err) {
    return err.toString();
  }

  private LogUtils() {
  }
}
