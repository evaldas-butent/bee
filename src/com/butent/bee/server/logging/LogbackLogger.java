package com.butent.bee.server.logging;

import com.butent.bee.shared.logging.BeeLogger;
import com.butent.bee.shared.logging.BeeLoggerWrapper;
import com.butent.bee.shared.logging.LogLevel;
import com.butent.bee.shared.utils.ArrayUtils;

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
    if (logger.isDebugEnabled()) {
      logInternal(LocationAwareLogger.DEBUG_INT, null, messages);
    }
  }

  @Override
  public void error(Throwable ex, Object... messages) {
    if (logger.isErrorEnabled()) {
      logInternal(LocationAwareLogger.ERROR_INT, ex, messages);
    }
  }

  @Override
  public void info(Object... messages) {
    if (logger.isInfoEnabled()) {
      logInternal(LocationAwareLogger.INFO_INT, null, messages);
    }
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
    if (logger.isErrorEnabled()) {
      logInternal(LocationAwareLogger.ERROR_INT, null, messages);
    }
  }

  @Override
  public void warning(Object... messages) {
    if (logger.isWarnEnabled()) {
      logInternal(LocationAwareLogger.WARN_INT, null, messages);
    }
  }

  private void logInternal(int level, Throwable ex, Object... messages) {
    logger.log(null, FQCN, level, ArrayUtils.joinWords(messages), null, ex);
  }
}
