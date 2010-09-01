package com.butent.bee.egg.server;

public class Assert extends com.butent.bee.egg.shared.Assert {
  public static void isInstanceOf(Class<?> clazz, Object obj) {
    isInstanceOf(clazz, obj, "");
  }

  public static void isInstanceOf(Class<?> type, Object obj, String message) {
    notNull(type, "Type to check against must not be null");
    if (!type.isInstance(obj)) {
      throw new IllegalArgumentException(message + "Object of class ["
          + (obj != null ? obj.getClass().getName() : "null")
          + "] must be an instance of " + type);
    }
  }

  public static void isAssignable(Class<?> superType, Class<?> subType) {
    isAssignable(superType, subType, "");
  }

  public static void isAssignable(Class<?> superType, Class<?> subType,
      String message) {
    notNull(superType, "Type to check against must not be null");
    if (subType == null || !superType.isAssignableFrom(subType)) {
      throw new IllegalArgumentException(message + subType
          + " is not assignable to " + superType);
    }
  }

}
