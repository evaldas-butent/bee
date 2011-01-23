package com.butent.bee.shared.data;

public interface HasCustomProperties {
  CustomProperties getProperties();
  Object getProperty(String key);

  void setProperties(CustomProperties properties);
  void setProperty(String propertyKey, Object propertyValue);
}
