package com.butent.bee.server.logging;

import com.google.common.base.Strings;

import com.butent.bee.shared.BeeConst;
import com.butent.bee.shared.logging.BeeLogger;
import com.butent.bee.shared.logging.BeeLoggerWrapper;
import com.butent.bee.shared.logging.LogLevel;
import com.butent.bee.shared.utils.ArrayUtils;
import com.butent.bee.shared.utils.BeeUtils;

import org.slf4j.LoggerFactory;
import org.slf4j.spi.LocationAwareLogger;

public class LogbackLogger implements BeeLogger {

  private static final String FQCN = BeeLoggerWrapper.class.getName();

  private final LocationAwareLogger logger;

  public LogbackLogger(String name) {
    logger = (LocationAwareLogger) LoggerFactory.getLogger(name);
  }

  @Override
  public void addSeparator() {
  }

  @Override
  public void debug(Object... messages) {
    if (isDebugEnabled()) {
      logInternal(LocationAwareLogger.DEBUG_INT, null, messages);
    }
  }

  @Override
  public void error(Throwable ex, Object... messages) {
    if (isErrorEnabled()) {
      if (ArrayUtils.length(messages) > 0) {
        severe(messages);
      }
      if (ex != null) {
        String sep = System.getProperty("line.separator");
        StringBuilder sb = new StringBuilder(ex.toString());
        int i = 0;
        boolean skip = false;

        for (StackTraceElement el : ex.getStackTrace()) {
          i++;
          if (BeeUtils.startsWith(el.getClassName(), "com.butent")
              && !BeeUtils.isEmpty(el.getFileName())) {
            skip = false;
            sb.append(sep)
                .append(BeeUtils.space(5)).append(BeeConst.STRING_LEFT_BRACKET)
                .append(Strings.padStart(Integer.toString(i), 3, BeeConst.CHAR_SPACE))
                .append(BeeConst.STRING_RIGHT_BRACKET).append(BeeConst.CHAR_SPACE)
                .append(el);
          } else if (!skip) {
            skip = true;
            sb.append(sep)
                .append(BeeUtils.space(5)).append("[...]");
          }
        }
        severe(sb.toString());
      }
      // logInternal(LocationAwareLogger.ERROR_INT, ex, messages);
    }
  }

  @Override
  public void info(Object... messages) {
    if (isInfoEnabled()) {
      logInternal(LocationAwareLogger.INFO_INT, null, messages);
    }
  }

  @Override
  public boolean isDebugEnabled() {
    return logger.isDebugEnabled();
  }

  @Override
  public boolean isErrorEnabled() {
    return logger.isErrorEnabled();
  }

  @Override
  public boolean isInfoEnabled() {
    return logger.isInfoEnabled();
  }

  @Override
  public boolean isWarningEnabled() {
    return logger.isWarnEnabled();
  }

  @Override
  public void log(LogLevel level, Object... messages) {
    switch (level) {
      case DEBUG:
        debug(messages);
        break;
      case ERROR:
        severe(messages);
        break;
      case INFO:
        info(messages);
        break;
      case WARNING:
        warning(messages);
        break;
    }
  }

  @Override
  public void severe(Object... messages) {
    if (isErrorEnabled()) {
      logInternal(LocationAwareLogger.ERROR_INT, null, messages);
    }
  }

  @Override
  public void warning(Object... messages) {
    if (isWarningEnabled()) {
      logInternal(LocationAwareLogger.WARN_INT, null, messages);
    }
  }

  private void logInternal(int level, Throwable ex, Object... messages) {
    logger.log(null, FQCN, level, ArrayUtils.joinWords(messages), null, ex);
  }
}
