package com.butent.bee.egg.client;

import com.google.gwt.core.client.GWT.UncaughtExceptionHandler;

import com.butent.bee.egg.shared.utils.LogUtils;

public class BeeExceptionHandler implements UncaughtExceptionHandler {

  @Override
  public void onUncaughtException(Throwable err) {
    LogUtils.error(err, "Uncaught Exception");
    Throwable cause = err.getCause();
    if (cause == null) {
      cause = err;
    }
    LogUtils.severe(cause.fillInStackTrace());
  }

}
