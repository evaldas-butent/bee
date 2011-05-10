package com.butent.bee.shared.data;

import com.butent.bee.shared.Assert;
import com.butent.bee.shared.data.value.BooleanValue;
import com.butent.bee.shared.data.value.NumberValue;
import com.butent.bee.shared.data.value.TextValue;
import com.butent.bee.shared.data.value.Value;
import com.butent.bee.shared.data.value.ValueType;

import java.util.Comparator;

/**
 * Implements {@code IsCell} interface, handles data cell functionality like changing value and
 * format of it.
 */

public class TableCell implements IsCell {

  public static Comparator<TableCell> getComparator() {
    return new Comparator<TableCell>() {
      @Override
      public int compare(TableCell cell1, TableCell cell2) {
        if (cell1 == cell2) {
          return 0;
        }
        if (cell1 == null) {
          return -1;
        }
        if (cell2 == null) {
          return 1;
        }
        return cell1.getValue().compareTo(cell2.getValue());
      }
    };
  }

  private Value value;
  private String formattedValue = null;
  private CustomProperties properties = null;

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

  public TableCell(Value value, String formattedValue, CustomProperties properties) {
    this.value = value;
    this.formattedValue = formattedValue;
    this.properties = properties;
  }

  public void clearFormattedValue() {
    setFormattedValue(null);
  }

  public void clearProperties() {
    setProperties(null);
  }

  public void clearValue() {
    setValue(Value.getNullValueFromValueType(getType()));
  }

  @Override
  public TableCell clone() {
    TableCell result = new TableCell(value, formattedValue);
    if (properties != null) {
      result.properties = properties.clone();
    }
    return result;
  }

  public String getFormattedValue() {
    return formattedValue;
  }

  public CustomProperties getProperties() {
    return properties;
  }

  public Object getProperty(String key) {
    Assert.notEmpty(key);
    if (properties == null) {
      return null;
    }
    return properties.get(key);
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

  public void setFormattedValue(String formattedValue) {
    this.formattedValue = formattedValue;
  }

  public void setProperties(CustomProperties properties) {
    this.properties = properties;
  }

  public void setProperty(String propertyKey, Object propertyValue) {
    Assert.notEmpty(propertyKey);
    Assert.notNull(propertyValue);
    if (properties == null) {
      properties = CustomProperties.create();
    }
    properties.put(propertyKey, propertyValue);
  }

  public void setValue(Value value) {
    this.value = value;
  }

  public void setValue(Value value, String formattedValue) {
    this.value = value;
    this.formattedValue = formattedValue;
  }

  @Override
  public String toString() {
    return value.toString();
  }
}
