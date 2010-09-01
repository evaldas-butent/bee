package com.butent.bee.egg.shared;

import java.util.Map;

import com.butent.bee.egg.shared.exceptions.ArgumentCountException;
import com.butent.bee.egg.shared.exceptions.ArgumentTypeException;
import com.butent.bee.egg.shared.exceptions.KeyNotFoundException;
import com.butent.bee.egg.shared.utils.BeeUtils;

public class Assert {
  public static void isTrue(boolean expression, String message) {
    if (!expression) {
      throw new IllegalArgumentException(message);
    }
  }

  public static void isTrue(boolean expression) {
    isTrue(expression, "[Assertion failed] - this expression must be true");
  }

  public static void isNull(Object object, String message) {
    if (object != null) {
      throw new IllegalArgumentException(message);
    }
  }

  public static void isNull(Object object) {
    isNull(object, "[Assertion failed] - the object argument must be null");
  }

  public static void notNull(Object object, String message) {
    if (object == null) {
      throw new IllegalArgumentException(message);
    }
  }

  public static void notNull(Object object) {
    notNull(object,
        "[Assertion failed] - this argument is required; it must not be null");
  }

  public static void notEmpty(Object obj, String message) {
    if (BeeUtils.isEmpty(obj)) {
      throw new IllegalArgumentException(message);
    }
  }

  public static void notEmpty(Object obj) {
    notEmpty(obj, "[Assertion failed] - argument must not be null or empty");
  }

  public static void noNullElements(String message, Object... obj) {
    for (int i = 0; i < obj.length; i++) {
      if (obj[i] == null) {
        throw new IllegalArgumentException(BeeUtils.concat(1, message,
            BeeUtils.bracket(i)));
      }
    }
  }

  public static void noNulls(Object... obj) {
    noNullElements("[Assertion failed] - arguments must not be null", obj);
  }

  public static void state(boolean expression, String message) {
    if (!expression) {
      throw new IllegalStateException(message);
    }
  }

  public static void state(boolean expression) {
    state(expression, "[Assertion failed] - this state invariant must be true");
  }

  public static void isPositive(int x, String msg) {
    if (x <= 0)
      throw new IllegalArgumentException(msg);
  }

  public static void isPositive(int x) {
    isPositive(x, "[Assertion failed] - argument must be positive");
  }

  public static void arrayLength(Object[] array, int min, int max) {
    notNull(array);

    if (min > 0 && array.length < min)
      throw new ArrayIndexOutOfBoundsException(min);
    if (max > 0 && array.length > max)
      throw new ArrayIndexOutOfBoundsException(max);
  }

  public static void arrayLength(Object[] array, int min) {
    arrayLength(array, min, -1);
  }

  public static void parameterCount(int c, int min, int max) {
    if (min > 0 && c < min || max > 0 && c > max)
      throw new ArgumentCountException(c, min, max);
  }

  public static void parameterCount(int c, int min) {
    parameterCount(c, min, -1);
  }

  public static <T> void contains(Map<T, ?> map, T key) {
    notNull(map);
    notNull(key);
    if (!map.containsKey(key))
      throw new KeyNotFoundException(key);
  }

  public static void isString(Object obj) {
    notNull(obj);
    if (!(obj instanceof String))
      throw new ArgumentTypeException(obj.getClass().getName(),
          String.class.getName());
  }

}
