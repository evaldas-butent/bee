package com.butent.bee.shared.data;

/**
 * Specifies implementing classes to be able to set and get their properties.
 */

public interface HasCustomProperties {

  void clearProperty(String key);

  CustomProperties getProperties();

  String getProperty(String key);

  Double getPropertyDouble(String key);

  Integer getPropertyInteger(String key);

  Long getPropertyLong(String key);

  void setProperties(CustomProperties properties);

  void setProperty(String key, Double value);

  void setProperty(String key, Integer value);

  void setProperty(String key, Long value);

  void setProperty(String key, String value);
}
