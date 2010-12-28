package com.butent.bee.egg.server.datasource.datatable;

import com.google.common.collect.Maps;

import com.butent.bee.egg.server.datasource.datatable.value.BooleanValue;
import com.butent.bee.egg.server.datasource.datatable.value.NumberValue;
import com.butent.bee.egg.server.datasource.datatable.value.TextValue;
import com.butent.bee.egg.server.datasource.datatable.value.Value;
import com.butent.bee.egg.server.datasource.datatable.value.ValueType;
import com.ibm.icu.util.ULocale;

import java.util.Collections;
import java.util.Comparator;
import java.util.Map;

public class TableCell {

  public static Comparator<TableCell> getLocalizedComparator(final ULocale ulocale) {
    return new Comparator<TableCell>() {
      private Comparator<TextValue> textValueComparator =
          TextValue.getTextLocalizedComparator(ulocale);

      @Override
      public int compare(TableCell cell1, TableCell cell2) {
        if (cell1 == cell2) {
          return 0;
        }
        if (cell1.getType() == ValueType.TEXT) {
          return textValueComparator.compare((TextValue) cell1.value,
              (TextValue) cell2.value);
        } else {
          return cell1.getValue().compareTo(cell2.getValue());
        }
      }
    };
  }
  private Value value;

  private String formattedValue = null;

  private Map<String, String> customProperties = null;

  public TableCell(boolean value) {
    this.value = BooleanValue.getInstance(value);
  }

  public TableCell(double value) {
    this.value = new NumberValue(value);
  }

  public TableCell(String value) {
    this.value = new TextValue(value);
  }

  public TableCell(TableCell other) {
    this(other.value, other.formattedValue);
  }

  public TableCell(Value value) {
    this.value = value;
  }

  public TableCell(Value value, String formattedValue) {
    this.value = value;
    this.formattedValue = formattedValue;
  }

  @Override
  public TableCell clone() {
    TableCell result = new TableCell(value, formattedValue);
    if (customProperties != null) {
      result.customProperties = Maps.newHashMap();
      for (Map.Entry<String, String> entry : customProperties.entrySet()) {
        result.customProperties.put(entry.getKey(), entry.getValue());
      }
    }
    return result;
  }

  public Map<String, String> getCustomProperties() {
    if (customProperties == null) {
      return Collections.emptyMap();
    }
    return Collections.unmodifiableMap(customProperties);
  }

  public String getCustomProperty(String key) {
    if (customProperties == null) {
      return null;
    }
    if (key == null) {
      throw new RuntimeException("Null keys are not allowed.");
    }
    return customProperties.get(key);
  }

  public String getFormattedValue() {
    return formattedValue;
  }

  public ValueType getType() {
    return value.getType();
  }

  public Value getValue() {
    return value;
  }

  public boolean isNull() {
    return value.isNull();
  }

  public void setCustomProperty(String propertyKey, String propertyValue) {
    if (customProperties == null) {
      customProperties = Maps.newHashMap();
    }
    if ((propertyKey == null) || (propertyValue == null)) {
      throw new RuntimeException("Null keys/values are not allowed.");
    }
    customProperties.put(propertyKey, propertyValue);
  }

  public void setFormattedValue(String formattedValue) {
    this.formattedValue = formattedValue;
  }

  @Override
  public String toString() {
    return value.toString();
  }
}
