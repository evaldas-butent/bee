package com.butent.bee.egg.server.datasource.datatable;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;

import com.butent.bee.egg.server.datasource.datatable.value.Value;

import java.util.Collections;
import java.util.List;
import java.util.Map;

public class TableRow {

  private List<TableCell> cells = Lists.newArrayList();
  private Map<String, String> customProperties = null;

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

  public void addCell(TableCell cell) {
    cells.add(cell);
  }

  public void addCell(Value value) {
    addCell(new TableCell(value));
  }

  @Override
  public TableRow clone() {
    TableRow result = new TableRow();
    for (TableCell cell : cells) {
      result.addCell(cell.clone());
    }
    if (customProperties != null) {
      result.customProperties = Maps.newHashMap();
      for (Map.Entry<String, String> entry : customProperties.entrySet()) {
        result.customProperties.put(entry.getKey(), entry.getValue());
      }
    }
    return result;
  }

  public TableCell getCell(int index) {
    return cells.get(index);
  }

  public List<TableCell> getCells() {
    return ImmutableList.copyOf(cells);
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

  public void setCustomProperty(String propertyKey, String propertyValue) {
    if (customProperties == null) {
      customProperties = Maps.newHashMap();
    }
    if ((propertyKey == null) || (propertyValue == null)) {
      throw new RuntimeException("Null keys/values are not allowed.");
    }
    customProperties.put(propertyKey, propertyValue);
  }
}
