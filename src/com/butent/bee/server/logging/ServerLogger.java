package com.butent.bee.server.logging;

import com.butent.bee.shared.logging.BeeLogger;
import com.butent.bee.shared.utils.BeeUtils;

import java.util.logging.Logger;

public class ServerLogger implements BeeLogger {
  private final Logger logger;

  public ServerLogger(String name) {
    logger = Logger.getLogger(name);
  }

  @Override
  public void debug(Object... messages) {
    logger.config(BeeUtils.concat(1, messages));
  }

  @Override
  public void error(Object... messages) {
    logger.severe(BeeUtils.concat(1, messages));
  }

  @Override
  public void info(Object... messages) {
    logger.info(BeeUtils.concat(1, messages));
  }

  @Override
  public void warning(Object... messages) {
    logger.warning(BeeUtils.concat(1, messages));
  }
}
