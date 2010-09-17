package com.butent.bee.egg.client;

import com.google.gwt.core.client.GWT.UncaughtExceptionHandler;

import com.butent.bee.egg.shared.utils.LogUtils;

public class BeeExceptionHandler implements UncaughtExceptionHandler {

  @Override
  public void onUncaughtException(Throwable err) {
    LogUtils.error(err.fillInStackTrace(), "Uncaught Exception");
    
    Throwable cause = err.getCause();
    int i = 0;
    while (cause != null && i++ < 100) {
      LogUtils.severe(cause);
      cause = cause.getCause();
    }
  }

}
