package com.butent.bee.client.grid.property;

import com.butent.bee.shared.Assert;

import java.util.HashMap;
import java.util.Map;

/**
 * Sets, gets and removes column properties.
 */

public class ColumnPropertyManager {
  private Map<String, ColumnProperty> properties = new HashMap<String, ColumnProperty>();

  @SuppressWarnings("unchecked")
  public <P extends ColumnProperty> P getColumnProperty(String name) {
    ColumnProperty property = properties.get(name);
    if (property == null) {
      return null;
    }
    return (P) property;
  }

  @SuppressWarnings("unchecked")
  public <P extends ColumnProperty> P removeColumnProperty(String name) {
    ColumnProperty property = properties.remove(name);
    if (property == null) {
      return null;
    }
    return (P) property;
  }

  public <P extends ColumnProperty> void setColumnProperty(String name, P property) {
    Assert.notEmpty(name, "Cannot add a property with no name");
    Assert.notNull(property, "Cannot add a null property");

    properties.put(name, property);
  }
}
