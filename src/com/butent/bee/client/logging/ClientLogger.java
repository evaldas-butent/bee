package com.butent.bee.client.logging;

import com.butent.bee.shared.logging.BeeLogger;
import com.butent.bee.shared.logging.LogLevel;
import com.butent.bee.shared.utils.ArrayUtils;

import java.util.logging.Handler;
import java.util.logging.Level;
import java.util.logging.Logger;

public class ClientLogger implements BeeLogger {
  
  private final Logger logger;

  public ClientLogger(String name) {
    this.logger = Logger.getLogger(name);
  }

  public void addHandler(Handler handler) {
    logger.addHandler(handler);
  }
  
  @Override
  public void addSeparator() {
    logger.log(LogFormatter.LOG_SEPARATOR_LEVEL, LogFormatter.LOG_SEPARATOR_TAG);
  }

  @Override
  public void debug(Object... messages) {
    if (logger.isLoggable(Level.CONFIG)) {
      logger.config(ArrayUtils.joinWords(messages));
    }
  }

  @Override
  public void error(Throwable ex, Object... messages) {
    if (logger.isLoggable(Level.SEVERE)) {
      logger.log(Level.SEVERE, ArrayUtils.joinWords(messages), ex);
    }
  }
  
  public PanelHandler getPanelHandler() {
    for (Handler handler : logger.getHandlers()) {
      if (handler instanceof PanelHandler) {
        return (PanelHandler) handler;
      }
    }
    return null;
  }

  @Override
  public void info(Object... messages) {
    if (logger.isLoggable(Level.INFO)) {
      logger.info(ArrayUtils.joinWords(messages));
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

  public void setLevel(Level lvl) {
    logger.setLevel(lvl);
  }

  @Override
  public void severe(Object... messages) {
    if (logger.isLoggable(Level.SEVERE)) {
      logger.severe(ArrayUtils.joinWords(messages));
    }
  }
  
  @Override
  public void warning(Object... messages) {
    if (logger.isLoggable(Level.WARNING)) {
      logger.warning(ArrayUtils.joinWords(messages));
    }
  }
}
