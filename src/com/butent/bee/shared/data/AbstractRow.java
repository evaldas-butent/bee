package com.butent.bee.shared.data;

import com.butent.bee.shared.Assert;
import com.butent.bee.shared.DateTime;
import com.butent.bee.shared.JustDate;
import com.butent.bee.shared.Transformable;
import com.butent.bee.shared.data.value.BooleanValue;
import com.butent.bee.shared.data.value.NumberValue;
import com.butent.bee.shared.data.value.TextValue;
import com.butent.bee.shared.data.value.Value;
import com.butent.bee.shared.utils.BeeUtils;

import java.util.List;

/**
 * Implements {@code isRow} interface, sets behaviors for row classes.
 */

public abstract class AbstractRow implements IsRow, Transformable {
  private long id;
  private CustomProperties properties = null;

  protected AbstractRow(long id) {
    this.id = id;
  }

  @SuppressWarnings("unused")
  private AbstractRow() {
  }

  public void addCell(boolean value) {
    addCell(new TableCell(value));
  }

  public void addCell(double value) {
    addCell(new TableCell(value));
  }

  public abstract void addCell(IsCell cell);

  public void addCell(String value) {
    addCell(new TableCell(value));
  }

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

  public Double getDouble(int index) {
    return getValue(index).getDouble();
  }

  public long getId() {
    return id;
  }

  public abstract int getNumberOfCells();

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

  public String getString(int index) {
    return getValue(index).getString();
  }

  public Value getValue(int index) {
    return getCell(index).getValue();
  }

  public abstract void insertCell(int index, IsCell cell);

  public boolean isNull(int index) {
    return getCell(index).isNull();
  }

  public abstract void removeCell(int index);

  public abstract void setCell(int index, IsCell cell);

  public abstract void setCells(List<IsCell> cells);

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

  public void setValue(int index, boolean value) {
    setValue(index, BooleanValue.getInstance(value));
  }

  public void setValue(int index, double value) {
    setValue(index, new NumberValue(value));
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

  protected void setId(long id) {
    this.id = id;
  }
}
