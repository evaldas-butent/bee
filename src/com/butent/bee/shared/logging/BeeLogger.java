package com.butent.bee.shared.logging;

import com.butent.bee.shared.logging.LogUtils.LogLevel;

public interface BeeLogger {

  void debug(Object... messages);

  void error(Object... messages);

  void info(Object... messages);

  void log(LogLevel level, Object... messages);

  void warning(Object... messages);
}
