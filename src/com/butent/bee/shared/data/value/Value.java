package com.butent.bee.shared.data.value;

import com.butent.bee.shared.Assert;
import com.butent.bee.shared.BeeConst;
import com.butent.bee.shared.BeeSerializable;
import com.butent.bee.shared.DateTime;
import com.butent.bee.shared.JustDate;
import com.butent.bee.shared.Transformable;
import com.butent.bee.shared.utils.BeeUtils;
import com.butent.bee.shared.utils.Codec;

import java.util.Date;

/**
 * Class {@code Value} is the root of the class hierarchy. {@code BooleanValue},
 * {@code DateTimeValue}, {@code DateValue}, {@code NumberValue}, 
 * {@code TextValue}, {@code TimeOfDayValue} has {@code Value} as a superclass.
 */
public abstract class Value implements Comparable<Value>, Transformable, BeeSerializable {

  public static Value getNullValueFromValueType(ValueType type) {
    switch (type) {
      case BOOLEAN:
        return BooleanValue.getNullValue();
      case TEXT:
        return TextValue.getNullValue();
      case NUMBER:
        return NumberValue.getNullValue();
      case TIMEOFDAY:
        return TimeOfDayValue.getNullValue();
      case DATE:
        return DateValue.getNullValue();
      case DATETIME:
        return DateTimeValue.getNullValue();
    }
    return null;
  }

  public static Value getValue(Object value) {
    Value val = null;
  
    if (value != null) {
      if (value instanceof Value) {
        val = (Value) value;
  
      } else if (value instanceof Boolean) {
        val = BooleanValue.getInstance((Boolean) value);
  
      } else if (value instanceof Number) {
        val = new NumberValue(((Number) value).doubleValue());
  
      } else if (value instanceof CharSequence) {
        val = new TextValue(value.toString());
  
      } else if (value instanceof Date) {
        val = new DateValue(new JustDate((Date) value));
  
      } else if (value instanceof JustDate) {
        val = new DateValue((JustDate) value);
  
      } else if (value instanceof DateTime) {
        val = new DateTimeValue((DateTime) value);
  
      } else {
        Assert.unsupported("Unsupported value type: " + BeeUtils.getClassName(value.getClass()));
      }
    }
    return val;
  }

  public static Value restore(String s) {
    String[] arr = Codec.beeDeserialize(s);
    Assert.lengthEquals(arr, 2);
    String clazz = arr[0];
    String data = arr[1];
    Value val = Value.getNullValue(clazz);
    Assert.notEmpty(val, "Unsupported class name: " + clazz);

    if (data != null) {
      if (BeeUtils.getClassName(BooleanValue.class).equals(clazz)) {
        val = BooleanValue.getInstance(BooleanValue.unpack(data));

      } else if (BeeUtils.getClassName(NumberValue.class).equals(clazz)) {
        val = new NumberValue(BeeUtils.toDouble(data));

      } else if (BeeUtils.getClassName(TextValue.class).equals(clazz)) {
        val = new TextValue(data);

      } else if (BeeUtils.getClassName(DateValue.class).equals(clazz)) {
        val = new DateValue(JustDate.parse(data));

      } else if (BeeUtils.getClassName(DateTimeValue.class).equals(clazz)) {
        val = new DateTimeValue(DateTime.parse(data));

      } else if (BeeUtils.getClassName(TimeOfDayValue.class).equals(clazz)) {
        val = new TimeOfDayValue(DateTime.parse(data));

      } else {
        Assert.untouchable("Unsupported class name: " + clazz);
      }
    }
    return val;
  }

  private static Value getNullValue(String clazz) {
    Value val = null;

    if (BeeUtils.getClassName(BooleanValue.class).equals(clazz)) {
      val = BooleanValue.getNullValue();

    } else if (BeeUtils.getClassName(NumberValue.class).equals(clazz)) {
      val = NumberValue.getNullValue();

    } else if (BeeUtils.getClassName(TextValue.class).equals(clazz)) {
      val = TextValue.getNullValue();

    } else if (BeeUtils.getClassName(DateValue.class).equals(clazz)) {
      val = DateValue.getNullValue();

    } else if (BeeUtils.getClassName(DateTimeValue.class).equals(clazz)) {
      val = DateTimeValue.getNullValue();

    } else if (BeeUtils.getClassName(TimeOfDayValue.class).equals(clazz)) {
      val = TimeOfDayValue.getNullValue();
    }
    return val;
  }
  
  public abstract int compareTo(Value o);

  @Override
  public void deserialize(String s) {
    Assert.unsupported();
  }
  
  @Override
  public boolean equals(Object o) {
    if (o == null || this.getClass() != o.getClass()) {
      return false;
    }
    return (this.compareTo((Value) o) == 0);
  }

  public Boolean getBoolean() {
    if (isNull()) {
      return null;
    }
    switch (getType()) {
      case BOOLEAN:
        return (Boolean) getObjectValue();
      case NUMBER:
        return !BeeUtils.isZero(getObjectValue());
      case TEXT:
        return BeeUtils.toBoolean((String) getObjectValue());
      default:
        return null;
    }
  }

  public JustDate getDate() {
    if (isNull()) {
      return null;
    }
    switch (getType()) {
      case DATE:
        return (JustDate) getObjectValue();
      case DATETIME:
        return new JustDate((DateTime) getObjectValue());
      case NUMBER:
        return new JustDate(((Number) getObjectValue()).intValue());
      default:
        return null;
    }
  }

  public DateTime getDateTime() {
    if (isNull()) {
      return null;
    }
    switch (getType()) {
      case DATE:
        return new DateTime((JustDate) getObjectValue());
      case DATETIME:
        return (DateTime) getObjectValue();
      case NUMBER:
        return new DateTime(((Number) getObjectValue()).longValue());
      default:
        return null;
    }
  }

  public Double getDouble() {
    if (isNull()) {
      return null;
    }
    switch (getType()) {
      case NUMBER:
        return (Double) getObjectValue();
      case TEXT:
        return BeeUtils.toDouble((String) getObjectValue());
      default:
        return null;
    }
  }

  public abstract Object getObjectValue();

  public String getString() {
    if (isNull()) {
      return null;
    }
    if (getType().equals(ValueType.TEXT)) {
      return (String) getObjectValue();
    }
    return transform();
  }

  public abstract ValueType getType();

  @Override
  public abstract int hashCode();

  public abstract boolean isNull();

  @Override
  public String serialize() {
    Object value = getObjectValue();

    if (value != null) {
      if (value instanceof Boolean) {
        value = BooleanValue.pack((Boolean) value);

      } else if (value instanceof JustDate) {
        value = ((JustDate) value).getDay();

      } else if (value instanceof DateTime) {
        value = ((DateTime) value).getTime();
      }
    }
    return Codec.beeSerializeAll(BeeUtils.getClassName(this.getClass()), value);
  }

  public String transform() {
    return toString();
  }

  protected int precompareTo(Value o) {
    int diff = BeeUtils.precompare(this, o);

    if (diff == BeeConst.COMPARE_UNKNOWN) {
      if (isNull()) {
        diff = o.isNull() ? BeeConst.COMPARE_EQUAL : BeeConst.COMPARE_LESS;
      } else if (o.isNull()) {
        diff = BeeConst.COMPARE_MORE;
      }
    }
    return diff;
  }
}
