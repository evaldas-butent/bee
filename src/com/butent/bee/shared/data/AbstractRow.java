package com.butent.bee.shared.data;

import com.butent.bee.shared.Assert;
import com.butent.bee.shared.data.value.BooleanValue;
import com.butent.bee.shared.data.value.NumberValue;
import com.butent.bee.shared.data.value.TextValue;
import com.butent.bee.shared.data.value.Value;

import java.util.List;

public abstract class AbstractRow implements IsRow {

  private CustomProperties properties = null;

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

  public Number getNumber(int index) {
    return getValue(index).getNumber();
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
  
  protected abstract void assertIndex(int index);
  
  protected void cloneProperties(IsRow cloneRow) {
    if (getProperties() != null) {
      cloneRow.setProperties(getProperties().clone());
    }
  }
}
