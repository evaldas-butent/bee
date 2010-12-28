package com.butent.bee.egg.server.datasource.datatable.value;

import com.google.common.collect.Maps;

import com.butent.bee.egg.server.datasource.base.TypeMismatchException;

import com.ibm.icu.util.GregorianCalendar;

import java.util.Map;

public enum ValueType {
  BOOLEAN("BOOLEAN"),
  NUMBER("NUMBER"),
  TEXT("STRING"),
  DATE("DATE"),
  TIMEOFDAY("TIMEOFDAY"),
  DATETIME("DATETIME");

  private static Map<String, ValueType> typeCodeToValueType;

  static {
    typeCodeToValueType = Maps.newHashMap();
    for (ValueType type : ValueType.values()) {
      typeCodeToValueType.put(type.typeCode, type);
    }
  }

  static ValueType getByTypeCode(String string) {
    return typeCodeToValueType.get(string);
  }

  private String typeCode;

  ValueType(String typeCode) {
    this.typeCode = typeCode;
  }

  public Value createValue(Object value) throws TypeMismatchException {
    Value ret = null;

    if (value == null) {
      ret = Value.getNullValueFromValueType(this);
    } else if ((this == TEXT) && (value instanceof String)) {
      ret = new TextValue((String) value);
    } else if ((this == NUMBER) && (value instanceof Number)) {
        ret = new NumberValue(((Number) value).doubleValue());
    } else if ((this == BOOLEAN) && (value instanceof Boolean)) {
        ret = ((Boolean) value).booleanValue() ? BooleanValue.TRUE : BooleanValue.FALSE;
    } else if ((this == DATE) && (value instanceof GregorianCalendar)) {
        ret = new DateValue((GregorianCalendar) value);
    } else if ((this == DATETIME) && (value instanceof GregorianCalendar)) {
        ret = new DateTimeValue((GregorianCalendar) value);
    } else if ((this == TIMEOFDAY) && (value instanceof GregorianCalendar)) {
        ret = new TimeOfDayValue((GregorianCalendar) value);
    }

    if (ret == null) {
      throw new TypeMismatchException("Value type mismatch.");
    }

    return ret;
  }

  public String getTypeCodeLowerCase() {
    return typeCode.toLowerCase();
  }

  String getTypeCode() {
    return typeCode;
  }
}
