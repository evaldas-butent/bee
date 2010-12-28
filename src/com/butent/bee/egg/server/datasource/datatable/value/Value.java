package com.butent.bee.egg.server.datasource.datatable.value;

import com.ibm.icu.util.ULocale;

import java.util.Comparator;

public abstract class Value implements Comparable<Value> {

  public static Comparator<Value> getLocalizedComparator(final ULocale ulocale) {
    return new Comparator<Value>() {
      private Comparator<TextValue> textValueComparator =
          TextValue.getTextLocalizedComparator(ulocale);

      @Override
      public int compare(Value value1, Value value2) {
        if (value1 == value2) {
          return 0;
        }
        if (value1.getType() == ValueType.TEXT) {
          return textValueComparator.compare((TextValue) value1,
              (TextValue) value2);
        } else {
          return value1.compareTo(value2);
        }
      }
    };
  }

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

  @Override
  public boolean equals(Object o) {
    if ((null == o) || (this.getClass() != o.getClass())) {
      return false;
    }
    return (this.compareTo((Value) o) == 0);
  }

  public abstract Object getObjectToFormat();

  public abstract ValueType getType();

  @Override
  public abstract int hashCode();

  public abstract boolean isNull();

  public final String toQueryString() {
    if (isNull()) {
      throw new RuntimeException("Cannot run toQueryString() on a null value.");
    }
    return innerToQueryString();
  }

  protected abstract String innerToQueryString();
}
