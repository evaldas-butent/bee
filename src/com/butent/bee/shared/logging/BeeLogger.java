package com.butent.bee.shared.logging;

public interface BeeLogger {

  void addSeparator();
  
  void debug(Object... messages);

  void error(Throwable ex, Object... messages);

  void info(Object... messages);

  void log(LogLevel level, Object... messages);

  void severe(Object... messages);

  void warning(Object... messages);
}
