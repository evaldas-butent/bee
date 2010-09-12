package com.butent.bee.egg.client;

import com.butent.bee.egg.shared.utils.LogUtils;
import com.google.gwt.core.client.GWT.UncaughtExceptionHandler;

public class BeeExceptionHandler implements UncaughtExceptionHandler {

  @Override
  public void onUncaughtException(Throwable err) {
    LogUtils.error(err, "Uncaught Exception");
    LogUtils.severe(err.getCause());
  }

}
