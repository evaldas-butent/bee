package com.butent.bee.client.ajaxloader;

import com.google.gwt.core.client.GWT;
import com.google.gwt.core.client.GWT.UncaughtExceptionHandler;

public abstract class ExceptionHelper {
  public static void runProtected(Runnable runnable) {
    UncaughtExceptionHandler handler = GWT.getUncaughtExceptionHandler();
    if (handler != null) {
      try {
        runnable.run();
      } catch (Throwable e) {
        handler.onUncaughtException(e);
      }
    } else {
      runnable.run();
    }
  }
  
  private ExceptionHelper() {
  }
}
