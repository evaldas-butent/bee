package com.butent.bee.client.logging;

import com.butent.bee.shared.logging.BeeLogger;
import com.butent.bee.shared.logging.LogUtils.LogLevel;
import com.butent.bee.shared.utils.BeeUtils;

import java.util.logging.Level;
import java.util.logging.Logger;

public class ClientLogger implements BeeLogger {
  private final Logger logger;

  public ClientLogger(String name) {
    logger = Logger.getLogger(name);
  }

  @Override
  public void debug(Object... messages) {
    if (logger.isLoggable(Level.CONFIG)) {
      logger.config(BeeUtils.joinWords(messages));
    }
  }

  @Override
  public void error(Object... messages) {
    if (logger.isLoggable(Level.SEVERE)) {
      logger.severe(BeeUtils.joinWords(messages));
    }
  }

  @Override
  public void info(Object... messages) {
    if (logger.isLoggable(Level.INFO)) {
      logger.info(BeeUtils.joinWords(messages));
    }
  }

  @Override
  public void log(LogLevel level, Object... messages) {
    switch (level) {
      case DEBUG:
        debug(messages);
        break;
      case ERROR:
        error(messages);
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
  public void warning(Object... messages) {
    if (logger.isLoggable(Level.WARNING)) {
      logger.warning(BeeUtils.joinWords(messages));
    }
  }
}
