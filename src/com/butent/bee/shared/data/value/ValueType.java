package com.butent.bee.shared.data.value;

import com.google.common.collect.Maps;

import com.butent.bee.shared.Assert;
import com.butent.bee.shared.utils.TimeUtils;

import java.math.BigDecimal;
import java.util.Map;

/**
 * The {@code ValueType} enumeration represents the types of Value. Creates a value of
 * {@code Object} type using {@link #createValue(Object)}.
 */
public enum ValueType {
  BOOLEAN("boolean"), NUMBER("double"), TEXT("string"),
  DATE("date"), TIMEOFDAY("timeofday"), DATETIME("datetime"),
  INTEGER("integer"), LONG("long"), DECIMAL("decimal");

  private static Map<String, ValueType> typeCodeToValueType;

  static {
    typeCodeToValueType = Maps.newHashMap();
    for (ValueType type : ValueType.values()) {
      typeCodeToValueType.put(type.typeCode.trim().toLowerCase(), type);
    }
  }

  public static ValueType getByTypeCode(String code) {
    if (code == null || code.isEmpty()) {
      return null;
    }
    return typeCodeToValueType.get(code.trim().toLowerCase());
  }

  public static boolean isNumber(ValueType type) {
    return type == NUMBER;
  }

  private String typeCode;

  ValueType(String typeCode) {
    this.typeCode = typeCode;
  }

  public Value createValue(Object value) {
    Value ret = null;

    if (value == null) {
      ret = Value.getNullValueFromValueType(this);
    } else if ((this == TEXT) && (value instanceof String)) {
      ret = new TextValue((String) value);
    } else if ((this == INTEGER) && (value instanceof Number)) {
      ret = new IntegerValue(((Number) value).intValue());
    } else if ((this == LONG) && (value instanceof Number)) {
      ret = new LongValue(((Number) value).longValue());
    } else if ((this == DECIMAL) && (value instanceof BigDecimal)) {
      ret = new DecimalValue(((BigDecimal) value));
    } else if ((this == NUMBER) && (value instanceof Number)) {
      ret = new NumberValue(((Number) value).doubleValue());
    } else if ((this == BOOLEAN) && (value instanceof Boolean)) {
      ret = ((Boolean) value).booleanValue() ? BooleanValue.TRUE : BooleanValue.FALSE;
    } else if ((this == DATE) && TimeUtils.isDateOrDateTime(value)) {
      ret = new DateValue(TimeUtils.toDate(value));
    } else if ((this == DATETIME) && TimeUtils.isDateOrDateTime(value)) {
      ret = new DateTimeValue(TimeUtils.toDateTime(value));
    } else if ((this == TIMEOFDAY) && (value instanceof String)) {
      ret = new TimeOfDayValue((String) value);
    }

    Assert.notNull(ret, "Value type mismatch.");
    return ret;
  }

  public String getTypeCode() {
    return typeCode;
  }
}
