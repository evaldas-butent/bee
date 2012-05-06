package com.butent.bee.shared.data;

import com.butent.bee.shared.Assert;
import com.butent.bee.shared.Transformable;
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
import java.util.List;

/**
 * Implements {@code isRow} interface, sets behaviors for row classes.
 */

public abstract class AbstractRow implements IsRow, Transformable {
  
  private long id;
  private long version = 0;

  private CustomProperties properties = null;

  protected AbstractRow(long id) {
    this.id = id;
  }

  @SuppressWarnings("unused")
  private AbstractRow() {
  }

  public abstract void addCell(IsCell cell);

  public void addCell(Value value) {
    addCell(new TableCell(value));
  }

  public abstract void clearCell(int index);

  @Override
  public abstract IsRow clone();

  public Boolean getBoolean(int index) {
    return getValue(index).getBoolean();
  }

  public abstract IsCell getCell(int index);

  public abstract List<IsCell> getCells();

  public JustDate getDate(int index) {
    return getValue(index).getDate();
  }

  public DateTime getDateTime(int index) {
    return getValue(index).getDateTime();
  }

  public BigDecimal getDecimal(int index) {
    return getValue(index).getDecimal();
  }

  public Double getDouble(int index) {
    return getValue(index).getDouble();
  }

  public long getId() {
    return id;
  }

  public Integer getInteger(int index) {
    return getValue(index).getInteger();
  }

  public Long getLong(int index) {
    return getValue(index).getLong();
  }

  public abstract int getNumberOfCells();

  public CustomProperties getProperties() {
    return properties;
  }

  public String getProperty(String key) {
    Assert.notEmpty(key);
    if (properties == null) {
      return null;
    }
    return properties.get(key);
  }

  public String getString(int index) {
    return getValue(index).getString();
  }

  public Value getValue(int index) {
    return getCell(index).getValue();
  }
  
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

  public long getVersion() {
    return version;
  }
  
  public abstract void insertCell(int index, IsCell cell);

  public boolean isNull(int index) {
    return getCell(index).isNull();
  }

  public abstract void removeCell(int index);

  public abstract void setCell(int index, IsCell cell);

  public abstract void setCells(List<IsCell> cells);

  public void setId(long id) {
    this.id = id;
  }

  public void setProperties(CustomProperties properties) {
    this.properties = properties;
  }

  public void setProperty(String propertyKey, String propertyValue) {
    Assert.notEmpty(propertyKey);
    Assert.notNull(propertyValue);
    if (properties == null) {
      properties = CustomProperties.create();
    }
    properties.put(propertyKey, propertyValue);
  }

  public void setValue(int index, BigDecimal value) {
    setValue(index, new DecimalValue(value));
  }

  public void setValue(int index, Boolean value) {
    setValue(index, BooleanValue.getInstance(value));
  }

  public void setValue(int index, DateTime value) {
    setValue(index, new DateTimeValue(value));
  }

  public void setValue(int index, Double value) {
    setValue(index, new NumberValue(value));
  }

  public void setValue(int index, Integer value) {
    setValue(index, new IntegerValue(value));
  }

  public void setValue(int index, JustDate value) {
    setValue(index, new DateValue(value));
  }

  public void setValue(int index, Long value) {
    setValue(index, new LongValue(value));
  }
  
  public void setValue(int index, String value) {
    setValue(index, new TextValue(value));
  }

  public void setValue(int index, Value value) {
    IsCell cell = getCell(index);
    cell.setValue(value);
    cell.clearFormattedValue();
    cell.clearProperties();
  }
  
  public void setVersion(long version) {
    this.version = version;
  }

  public String transform() {
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
      sb.append("p=").append(getProperties().transform());
    }
    return sb.toString();
  }

  protected abstract void assertIndex(int index);

  protected void cloneProperties(IsRow cloneRow) {
    if (getProperties() != null) {
      cloneRow.setProperties(getProperties().clone());
    }
  }
}
