package com.butent.bee.shared.utils;

import com.butent.bee.shared.BeeConst;
import com.butent.bee.shared.HasDoubleValue;
import com.butent.bee.shared.HasIntValue;
import com.butent.bee.shared.HasLongValue;

public class ValueUtils {
  public static double getDouble(Object obj) {
    if (obj == null) {
      return BeeConst.DOUBLE_ZERO;
    }
    
    if (obj instanceof Number) {
      return ((Number) obj).doubleValue();
    }
    if (obj instanceof String) {
      return Double.valueOf((String) obj);
    }
    if (obj instanceof Boolean) {
      return BeeUtils.toInt((Boolean) obj);
    }
    if (obj instanceof Character) {
      return Double.valueOf((Character) obj);
    }
    
    if (obj instanceof HasDoubleValue) {
      return ((HasDoubleValue) obj).getDouble();
    }
    if (obj instanceof HasLongValue) {
      return ((HasLongValue) obj).getLong();
    }
    if (obj instanceof HasIntValue) {
      return ((HasIntValue) obj).getInt();
    }
    return BeeConst.DOUBLE_ZERO;
  }
  
  public static int getInt(Object obj) {
    if (obj == null) {
      return 0;
    }
    
    if (obj instanceof Number) {
      return ((Number) obj).intValue();
    }
    if (obj instanceof String) {
      return Integer.valueOf((String) obj);
    }
    if (obj instanceof Boolean) {
      return BeeUtils.toInt((Boolean) obj);
    }
    if (obj instanceof Character) {
      return (Integer) obj;
    }
    
    if (obj instanceof HasIntValue) {
      return ((HasIntValue) obj).getInt();
    }
    return 0;
  }

  public static long getLong(Object obj) {
    if (obj == null) {
      return 0;
    }
    
    if (obj instanceof Number) {
      return ((Number) obj).longValue();
    }
    if (obj instanceof String) {
      return Long.valueOf((String) obj);
    }
    if (obj instanceof Boolean) {
      return BeeUtils.toInt((Boolean) obj);
    }
    if (obj instanceof Character) {
      return (Long) obj;
    }
    
    if (obj instanceof HasLongValue) {
      return ((HasLongValue) obj).getLong();
    }
    if (obj instanceof HasIntValue) {
      return ((HasIntValue) obj).getInt();
    }
    return 0;
  }

  public static Object setDouble(Object obj, double value) {
    if (obj == null) {
      return obj;
    }
    
    if (obj instanceof Number) {
      return value;
    }
    if (obj instanceof String) {
      return Double.toString(value);
    }
    if (obj instanceof Boolean) {
      return ((Double) value).equals(BeeConst.INT_TRUE);
    }
    if (obj instanceof Character) {
      return (char) value;
    }
    
    if (obj instanceof HasDoubleValue) {
      ((HasDoubleValue) obj).setValue(value);
      return obj;
    }
    if (obj instanceof HasLongValue) {
      ((HasLongValue) obj).setValue((long) value);
      return obj;
    }
    if (obj instanceof HasIntValue) {
      ((HasIntValue) obj).setValue((int) value);
      return obj;
    }
    return obj;
  }

  public static Object setInt(Object obj, int value) {
    if (obj == null) {
      return obj;
    }
    
    if (obj instanceof Number) {
      return value;
    }
    if (obj instanceof String) {
      return Integer.toString(value);
    }
    if (obj instanceof Boolean) {
      return BeeUtils.toBoolean(value);
    }
    if (obj instanceof Character) {
      return (char) value;
    }
    
    if (obj instanceof HasIntValue) {
      ((HasIntValue) obj).setValue(value);
      return obj;
    }
    return obj;
  }

  public static Object setLong(Object obj, long value) {
    if (obj == null) {
      return obj;
    }
    
    if (obj instanceof Number) {
      return value;
    }
    if (obj instanceof String) {
      return Long.toString(value);
    }
    if (obj instanceof Boolean) {
      return value == BeeConst.INT_TRUE;
    }
    if (obj instanceof Character) {
      return (char) value;
    }
    
    if (obj instanceof HasLongValue) {
      ((HasLongValue) obj).setValue(value);
      return obj;
    }
    if (obj instanceof HasIntValue) {
      ((HasIntValue) obj).setValue((int) value);
      return obj;
    }
    return obj;
  }
}
