package com.butent.bee.egg.shared.data;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.Lists;

import com.butent.bee.egg.shared.Assert;
import com.butent.bee.egg.shared.data.value.Value;

import java.util.List;

public class TableRow implements IsRow {

  private List<IsCell> cells = Lists.newArrayList();
  private CustomProperties properties = null;

  public TableRow() {
  }

  public void addCell(boolean value) {
    addCell(new TableCell(value));
  }

  public void addCell(double value) {
    addCell(new TableCell(value));
  }

  public void addCell(String value) {
    addCell(new TableCell(value));
  }

  public void addCell(IsCell cell) {
    cells.add(cell);
  }

  public void addCell(Value value) {
    addCell(new TableCell(value));
  }

  @Override
  public TableRow clone() {
    TableRow result = new TableRow();
    for (IsCell cell : cells) {
      result.addCell(cell.clone());
    }
    if (properties != null) {
      result.properties = properties.clone();
    }
    return result;
  }

  public IsCell getCell(int index) {
    return cells.get(index);
  }

  public List<IsCell> getCells() {
    return ImmutableList.copyOf(cells);
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

  public void setCells(List<IsCell> cells) {
    this.cells = cells;
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
}
