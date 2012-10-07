package com.butent.bee.shared.logging;

public class BeeLoggerWrapper implements BeeLogger {

  private final String loggerName;
  private BeeLogger logger;

  public BeeLoggerWrapper(String name) {
    this.loggerName = name;
  }

  @Override
  public void addSeparator() {
    if (initLogger()) {
      logger.addSeparator();
    }
  }

  @Override
  public void debug(Object... messages) {
    if (initLogger()) {
      logger.debug(messages);
    }
  }

  @Override
  public void error(Throwable ex, Object... messages) {
    if (initLogger()) {
      logger.error(ex, messages);
    }
  }

  @Override
  public void info(Object... messages) {
    if (initLogger()) {
      logger.info(messages);
    }
  }

  @Override
  public void log(LogLevel level, Object... messages) {
    if (initLogger()) {
      logger.log(level, messages);
    }
  }

  @Override
  public void severe(Object... messages) {
    if (initLogger()) {
      logger.severe(messages);
    }
  }

  @Override
  public void warning(Object... messages) {
    if (initLogger()) {
      logger.warning(messages);
    }
  }

  private boolean initLogger() {
    if (logger == null) {
      logger = LogUtils.createLogger(loggerName);
    }
    return (logger != null);
  }
}
