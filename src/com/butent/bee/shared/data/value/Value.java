package com.butent.bee.shared.data.value;

import com.butent.bee.shared.Assert;
import com.butent.bee.shared.BeeConst;
import com.butent.bee.shared.BeeSerializable;
import com.butent.bee.shared.DateTime;
import com.butent.bee.shared.JustDate;
import com.butent.bee.shared.Transformable;
import com.butent.bee.shared.utils.BeeUtils;
import com.butent.bee.shared.utils.Codec;
import com.butent.bee.shared.utils.TimeUtils;

import java.math.BigDecimal;
import java.util.Date;

/**
 * Class {@code Value} is the root of the class hierarchy. {@code BooleanValue},
 * {@code DateTimeValue}, {@code DateValue}, {@code NumberValue}, {@code TextValue},
 * {@code TimeOfDayValue} has {@code Value} as a superclass.
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
      case INTEGER:
        return IntegerValue.getNullValue();
      case LONG:
        return LongValue.getNullValue();
      case DECIMAL:
        return DecimalValue.getNullValue();
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

      } else if (value instanceof Integer) {
        val = new IntegerValue(((Integer) value));

      } else if (value instanceof Long) {
        val = new LongValue(((Long) value));

      } else if (value instanceof BigDecimal) {
        val = new DecimalValue(((BigDecimal) value));

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

  public static Value parseValue(ValueType type, String value) {
    Assert.notNull(type, "value type not specified");
    if (value == null) {
      return getNullValueFromValueType(type);
    }

    switch (type) {
      case BOOLEAN:
        return new BooleanValue(BeeUtils.toBooleanOrNull(value));
      case TEXT:
        return new TextValue(value);
      case NUMBER:
        return new NumberValue(BeeUtils.toDoubleOrNull(value));
      case TIMEOFDAY:
        return new TimeOfDayValue(value);
      case DATE:
        return new DateValue(TimeUtils.toDateOrNull(value));
      case DATETIME:
        return new DateTimeValue(TimeUtils.toDateTimeOrNull(value));
      case INTEGER:
        return new IntegerValue(BeeUtils.toIntOrNull(value));
      case LONG:
        return new LongValue(BeeUtils.toLongOrNull(value));
      case DECIMAL:
        return new DecimalValue(BeeUtils.toDecimalOrNull(value));
    }
    return null;
  }

  public static Value restore(String s) {
    String[] arr = Codec.beeDeserialize(s);
    Assert.lengthEquals(arr, 2);
    String clazz = arr[0];
    String data = arr[1];
    
    ValueType type = ValueType.getByTypeCode(clazz);
    Assert.notNull(type, "Unsupported value type: " + clazz);
    
    return parseValue(type, data);
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

  public abstract Boolean getBoolean();

  public abstract JustDate getDate();

  public abstract DateTime getDateTime();

  public abstract BigDecimal getDecimal();

  public abstract Double getDouble();

  public abstract Integer getInteger();

  public abstract Long getLong();

  public abstract Object getObjectValue();

  public abstract String getString();

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

  public abstract String transform();

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
