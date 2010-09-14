package com.butent.bee.egg.shared;

import com.butent.bee.egg.shared.exceptions.ArgumentCountException;
import com.butent.bee.egg.shared.exceptions.ArgumentTypeException;
import com.butent.bee.egg.shared.exceptions.BeeRuntimeException;
import com.butent.bee.egg.shared.exceptions.KeyNotFoundException;
import com.butent.bee.egg.shared.utils.BeeUtils;

import java.util.Map;

public class Assert {
  public static final String ASSERTION_FAILED = "[Assertion failed] - ";

  public static void arrayLength(Object[] array, int min) {
    arrayLength(array, min, -1);
  }

  public static void arrayLength(Object[] array, int min, int max) {
    notNull(array);
    int len = array.length;

    if (min > 0 && len < min) {
      throw new BeeRuntimeException(ASSERTION_FAILED + "array length " + len
          + " must be >= " + min);
    }
    if (max > 0 && len > max) {
      throw new BeeRuntimeException(ASSERTION_FAILED + "array length " + len
          + " must be <= " + max);
    }
  }

  public static <T> void contains(Map<T, ?> map, T key) {
    notNull(map);
    notNull(key);
    if (!map.containsKey(key)) {
      throw new KeyNotFoundException(key);
    }
  }

  public static void isNull(Object object) {
    isNull(object, ASSERTION_FAILED + "the object argument must be null");
  }

  public static void isNull(Object object, String message) {
    if (object != null) {
      throw new BeeRuntimeException(message);
    }
  }

  public static void isPositive(int x) {
    isPositive(x, ASSERTION_FAILED + "argument must be positive");
  }

  public static void isPositive(int x, String msg) {
    if (x <= 0) {
      throw new BeeRuntimeException(msg);
    }
  }

  public static void isString(Object obj) {
    notNull(obj);
    if (!(obj instanceof String)) {
      throw new ArgumentTypeException(obj.getClass().getName(),
          String.class.getName());
    }
  }

  public static void isTrue(boolean expression) {
    isTrue(expression, ASSERTION_FAILED + "this expression must be true");
  }

  public static void isTrue(boolean expression, String message) {
    if (!expression) {
      throw new BeeRuntimeException(message);
    }
  }

  public static void noNullElements(String message, Object... obj) {
    for (int i = 0; i < obj.length; i++) {
      if (obj[i] == null) {
        throw new BeeRuntimeException(BeeUtils.concat(1, message,
            BeeUtils.bracket(i)));
      }
    }
  }

  public static void noNulls(Object... obj) {
    noNullElements(ASSERTION_FAILED + "arguments must not be null", obj);
  }

  public static void notEmpty(Object obj) {
    notEmpty(obj, ASSERTION_FAILED + "argument must not be null or empty");
  }

  public static void notEmpty(Object obj, String message) {
    if (BeeUtils.isEmpty(obj)) {
      throw new BeeRuntimeException(message);
    }
  }

  public static void notNull(Object object) {
    notNull(object, ASSERTION_FAILED
        + "this argument is required; it must not be null");
  }

  public static void notNull(Object object, String message) {
    if (object == null) {
      throw new BeeRuntimeException(message);
    }
  }

  public static void parameterCount(int c, int min) {
    parameterCount(c, min, -1);
  }

  public static void parameterCount(int c, int min, int max) {
    if (min > 0 && c < min || max > 0 && c > max) {
      throw new ArgumentCountException(c, min, max);
    }
  }

  public static void state(boolean expression) {
    state(expression, ASSERTION_FAILED + "this state invariant must be true");
  }

  public static void state(boolean expression, String message) {
    if (!expression) {
      throw new BeeRuntimeException(message);
    }
  }

}
