package com.butent.bee.shared.logging;

import com.butent.bee.shared.utils.LogUtils;

public class BeeLoggerWrapper implements BeeLogger {

  private final String loggerName;
  private BeeLogger logger;

  public BeeLoggerWrapper(String name) {
    this.loggerName = name;
  }

  @Override
  public void debug(Object... messages) {
    getLogger().debug(messages);
  }

  @Override
  public void error(Object... messages) {
    getLogger().error(messages);
  }

  @Override
  public void info(Object... messages) {
    getLogger().info(messages);
  }

  @Override
  public void warning(Object... messages) {
    getLogger().warning(messages);
  }

  private BeeLogger getLogger() {
    if (logger == null) {
      logger = LogUtils.createLogger(loggerName);
    }
    return logger;
  }
}
