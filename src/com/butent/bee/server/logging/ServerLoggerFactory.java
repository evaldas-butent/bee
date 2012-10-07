package com.butent.bee.server.logging;

import com.butent.bee.shared.logging.BeeLogger;
import com.butent.bee.shared.logging.BeeLoggerFactory;
import com.butent.bee.shared.logging.BeeLoggerWrapper;

public class ServerLoggerFactory implements BeeLoggerFactory {

  @Override
  public BeeLogger createLogger(String name) {
    return ServerLogger.create(name);
  }
  
  @Override
  public BeeLogger getLogger(String name) {
    return new BeeLoggerWrapper(name);
  }
}
