package com.butent.bee.shared.logging;

public interface BeeLogger {

  void debug(Object... messages);

  void error(Object... messages);

  void info(Object... messages);

  void warning(Object... messages);
}
