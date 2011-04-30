package com.butent.bee.server.i18n;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.util.Properties;

/**
 * Contains method requirements for any classes that can be localized.
 */

public abstract class GwtLocalizable implements InvocationHandler {

  private Properties properties = new Properties();

  GwtLocalizable(Properties properties) {
    this.properties = properties;
  }

  public abstract Object invoke(Object proxy, Method method, Object[] args) throws Throwable;

  protected String getProperty(String key) {
    return properties.getProperty(key);
  }
}
