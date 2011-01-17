package com.butent.bee.client;

import com.google.gwt.core.client.GWT.UncaughtExceptionHandler;

import com.butent.bee.client.logging.LogFormatter;
import com.butent.bee.shared.utils.LogUtils;

public class BeeExceptionHandler implements UncaughtExceptionHandler {
  public void onUncaughtException(Throwable err) {
    LogUtils.error(err.fillInStackTrace(), "Uncaught Exception");
    
    Throwable cause = err.getCause();
    int i = 0;
    while (cause != null && i++ < 100) {
      LogUtils.severe(cause);
      cause = cause.getCause();
    }
    
    LogUtils.log(LogFormatter.LOG_SEPARATOR_LEVEL, LogFormatter.LOG_SEPARATOR_TAG);
  }
}
