package com.butent.bee.client.logging;

import com.butent.bee.shared.logging.BeeLogger;
import com.butent.bee.shared.logging.BeeLoggerFactory;

public class ClientLoggerFactory implements BeeLoggerFactory {

  @Override
  public BeeLogger getLogger(String name) {
    return new ClientLogger(name);
  }
}
