package com.butent.bee.shared.data;

/**
 * Specifies implementing classes to be able to set and get their properties.
 */

public interface HasCustomProperties {
  CustomProperties getProperties();

  String getProperty(String key);

  void setProperties(CustomProperties properties);

  void setProperty(String propertyKey, String propertyValue);
}
