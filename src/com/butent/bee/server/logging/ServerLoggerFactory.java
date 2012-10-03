package com.butent.bee.server.logging;

import com.butent.bee.shared.logging.BeeLogger;
import com.butent.bee.shared.logging.BeeLoggerFactory;

public class ServerLoggerFactory implements BeeLoggerFactory {

  @Override
  public BeeLogger getLogger(String name) {
    return ServerLogger.create(name);
  }
}
