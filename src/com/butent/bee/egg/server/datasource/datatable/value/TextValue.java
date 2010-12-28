package com.butent.bee.egg.server.datasource.datatable.value;

import com.ibm.icu.text.Collator;
import com.ibm.icu.util.ULocale;

import java.util.Comparator;

public class TextValue extends Value {

  private static final TextValue NULL_VALUE = new TextValue("");

  public static TextValue getNullValue() {
    return NULL_VALUE;
  }

  public static Comparator<TextValue> getTextLocalizedComparator(final ULocale ulocale) {
    return new Comparator<TextValue>() {
      Collator collator = Collator.getInstance(ulocale);

      @Override
      public int compare(TextValue tv1, TextValue tv2) {
        if (tv1 == tv2) {
          return 0;
        }
        return collator.compare(tv1.value, tv2.value);
      }
    };
  }

  private String value;

  public TextValue(String value) {
    if (value == null) {
      throw new NullPointerException("Cannot create a text value from null.");
    }
    this.value = value;
  }

  @Override
  public int compareTo(Value other) {
    if (this == other) {
      return 0;
    }
    return value.compareTo(((TextValue) other).value);
  }

  @Override
  public String getObjectToFormat() {
    return value;
  }

  @Override
  public ValueType getType() {
    return ValueType.TEXT;
  }

  public String getValue() {
    return value;
  }

  @Override
  public int hashCode() {
    return value.hashCode();
  }

  @Override
  public boolean isNull() {
    return (this == NULL_VALUE);
  }

  @Override
  public String toString() {
    return value;
  }

  @Override
  protected String innerToQueryString() {
    if (value.contains("\"")) {
      if (value.contains("'")) {
        throw new RuntimeException("Cannot run toQueryString() on string"
            + " values that contain both \" and '.");
      } else {
        return "'" + value + "'";
      }
    } else {
      return "\"" + value + "\"";
    }
  }
}
