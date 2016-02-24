package com.butent.bee.shared.data;

import com.butent.bee.shared.utils.BeeUtils;

/**
 * Specifies implementing classes to be able to set and get their properties.
 */

public interface HasCustomProperties {

  char USER_SEPARATOR = '¦';

  void clearProperty(String key);

  default void clearProperty(String key, Long userId) {
    clearProperty(userPropertyName(key, userId));
  }

  default boolean containsProperty(String key) {
    return !BeeUtils.isEmpty(getProperty(key));
  }

  default boolean containsProperty(String key, Long userId) {
    return containsProperty(userPropertyName(key, userId));
  }

  static String extractPropertyNameFromUserPropertyName(String key) {
    return BeeUtils.getPrefix(key, USER_SEPARATOR);
  }

  static Long extractUserIdFromUserPropertyName(String key) {
    return BeeUtils.toLongOrNull(BeeUtils.getSuffix(key, USER_SEPARATOR));
  }

  CustomProperties getProperties();

  String getProperty(String key);

  default String getProperty(String key, Long userId) {
    return getProperty(userPropertyName(key, userId));
  }

  default Double getPropertyDouble(String key) {
    return BeeUtils.toDoubleOrNull(getProperty(key));
  }

  default Integer getPropertyInteger(String key) {
    return BeeUtils.toIntOrNull(getProperty(key));
  }

  default Long getPropertyLong(String key) {
    return BeeUtils.toLongOrNull(getProperty(key));
  }

  default Double getPropertyDouble(String key, Long userId) {
    return getPropertyDouble(userPropertyName(key, userId));
  }

  default Integer getPropertyInteger(String key, Long userId) {
    return getPropertyInteger(userPropertyName(key, userId));
  }

  default Long getPropertyLong(String key, Long userId) {
    return getPropertyLong(userPropertyName(key, userId));
  }

  static boolean isUserPropertyName(String key) {
    return BeeUtils.count(key, USER_SEPARATOR) == 1
        && DataUtils.isId(BeeUtils.getSuffix(key, USER_SEPARATOR));
  }

  void setProperties(CustomProperties properties);

  default void setProperty(String key, Double value) {
    String s = BeeUtils.isDouble(value) ? BeeUtils.toString(value) : null;
    setProperty(key, s);
  }

  default void setProperty(String key, Integer value) {
    String s = (value == null) ? null : BeeUtils.toString(value);
    setProperty(key, s);
  }

  default void setProperty(String key, Long value) {
    String s = (value == null) ? null : BeeUtils.toString(value);
    setProperty(key, s);
  }

  void setProperty(String key, String value);

  default void setProperty(String key, Long userId, Double value) {
    setProperty(userPropertyName(key, userId), value);
  }

  default void setProperty(String key, Long userId, Integer value) {
    setProperty(userPropertyName(key, userId), value);
  }

  default void setProperty(String key, Long userId, Long value) {
    setProperty(userPropertyName(key, userId), value);
  }

  default void setProperty(String key, Long userId, String value) {
    setProperty(userPropertyName(key, userId), value);
  }

  static String userPropertyName(String key, Long userId) {
    if (!BeeUtils.isEmpty(key) && DataUtils.isId(userId)) {
      return key.trim() + USER_SEPARATOR + userId;
    } else {
      return key;
    }
  }
}
