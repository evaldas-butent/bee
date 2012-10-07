package com.butent.bee.shared.logging;

public interface BeeLoggerFactory {

  BeeLogger createLogger(String name);

  BeeLogger getLogger(String name);
}
