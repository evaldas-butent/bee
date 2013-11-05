package com.butent.bee.shared.logging;

public interface BeeLoggerFactory {

  void stop();

  BeeLogger getLogger(String name);

  BeeLogger getRootLogger();
}
