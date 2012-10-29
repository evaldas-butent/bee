package com.butent.bee.shared.data;

import com.butent.bee.shared.Assert;
import com.butent.bee.shared.data.value.BooleanValue;
import com.butent.bee.shared.data.value.DateTimeValue;
import com.butent.bee.shared.data.value.DateValue;
import com.butent.bee.shared.data.value.DecimalValue;
import com.butent.bee.shared.data.value.IntegerValue;
import com.butent.bee.shared.data.value.LongValue;
import com.butent.bee.shared.data.value.NumberValue;
import com.butent.bee.shared.data.value.TextValue;
import com.butent.bee.shared.data.value.TimeOfDayValue;
import com.butent.bee.shared.data.value.Value;
import com.butent.bee.shared.data.value.ValueType;
import com.butent.bee.shared.time.DateTime;
import com.butent.bee.shared.time.JustDate;
import com.butent.bee.shared.utils.BeeUtils;

import java.math.BigDecimal;

/**
 * Implements {@code isRow} interface, sets behaviors for row classes.
 */

public abstract class AbstractRow implements IsRow {

  private long id;
  private long version = 0;

  private CustomProperties properties = null;

  protected AbstractRow(long id) {
    this.id = id;
  }

  @Override
  public void addCell(Value value) {
    addCell(new TableCell(value));
  }

  @Override
  public void clearProperty(String key) {
    Assert.notEmpty(key);
    if (properties != null) {
      properties.remove(key);
    }
  }

  @Override
  public Boolean getBoolean(int index) {
    return getValue(index).getBoolean();
  }

  @Override
  public JustDate getDate(int index) {
    return getValue(index).getDate();
  }

  @Override
  public DateTime getDateTime(int index) {
    return getValue(index).getDateTime();
  }

  @Override
  public BigDecimal getDecimal(int index) {
    return getValue(index).getDecimal();
  }

  @Override
  public Double getDouble(int index) {
    return getValue(index).getDouble();
  }

  @Override
  public long getId() {
    return id;
  }

  @Override
  public Integer getInteger(int index) {
    return getValue(index).getInteger();
  }

  @Override
  public Long getLong(int index) {
    return getValue(index).getLong();
  }

  @Override
  public CustomProperties getProperties() {
    return properties;
  }

  @Override
  public String getProperty(String key) {
    Assert.notEmpty(key);
    if (properties == null) {
      return null;
    }
    return properties.get(key);
  }

  @Override
  public String getString(int index) {
    return getValue(index).getString();
  }

  @Override
  public Value getValue(int index) {
    return getCell(index).getValue();
  }

  @Override
  public Value getValue(int index, ValueType type) {
    Assert.notNull(type, "value type not specified");

    switch (type) {
      case BOOLEAN:
        return new BooleanValue(getBoolean(index));
      case DATE:
        return new DateValue(getDate(index));
      case DATETIME:
        return new DateTimeValue(getDateTime(index));
      case NUMBER:
        return new NumberValue(getDouble(index));
      case TEXT:
        return new TextValue(getString(index));
      case TIMEOFDAY:
        return new TimeOfDayValue(getString(index));
      case INTEGER:
        return new IntegerValue(getInteger(index));
      case LONG:
        return new LongValue(getLong(index));
      case DECIMAL:
        return new DecimalValue(getDecimal(index));
    }
    return null;
  }

  @Override
  public long getVersion() {
    return version;
  }

  @Override
  public boolean isNull(int index) {
    return getCell(index).isNull();
  }

  @Override
  public void setId(long id) {
    this.id = id;
  }

  @Override
  public void setProperties(CustomProperties properties) {
    this.properties = properties;
  }

  @Override
  public void setProperty(String propertyKey, String propertyValue) {
    Assert.notEmpty(propertyKey);

    if (BeeUtils.isEmpty(propertyValue)) {
      clearProperty(propertyKey);
    } else {
      if (properties == null) {
        properties = CustomProperties.create();
      }
      properties.put(propertyKey, propertyValue);
    }
  }

  @Override
  public void setValue(int index, BigDecimal value) {
    setValue(index, new DecimalValue(value));
  }

  @Override
  public void setValue(int index, Boolean value) {
    setValue(index, BooleanValue.getInstance(value));
  }

  @Override
  public void setValue(int index, DateTime value) {
    setValue(index, new DateTimeValue(value));
  }

  @Override
  public void setValue(int index, Double value) {
    setValue(index, new NumberValue(value));
  }

  @Override
  public void setValue(int index, Integer value) {
    setValue(index, new IntegerValue(value));
  }

  @Override
  public void setValue(int index, JustDate value) {
    setValue(index, new DateValue(value));
  }

  @Override
  public void setValue(int index, Long value) {
    setValue(index, new LongValue(value));
  }

  @Override
  public void setValue(int index, String value) {
    setValue(index, new TextValue(value));
  }

  @Override
  public void setValue(int index, Value value) {
    IsCell cell = getCell(index);
    cell.setValue(value);
    cell.clearFormattedValue();
    cell.clearProperties();
  }

  @Override
  public void setVersion(long version) {
    this.version = version;
  }

  @Override
  public String toString() {
    StringBuilder sb = new StringBuilder();
    sb.append("id=").append(getId());

    String v;
    for (int i = 0; i < getNumberOfCells(); i++) {
      v = getString(i);
      if (!BeeUtils.isEmpty(v)) {
        sb.append(" [").append(i).append("]=").append(v);
      }
    }

    if (getProperties() != null) {
      sb.append("p=").append(getProperties());
    }
    return sb.toString();
  }

  protected void copyProperties(IsRow target) {
    if (getProperties() != null) {
      target.setProperties(getProperties().copy());
    }
  }
}
