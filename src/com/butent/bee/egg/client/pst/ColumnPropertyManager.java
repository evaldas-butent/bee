package com.butent.bee.egg.client.pst;

import com.butent.bee.egg.client.pst.ColumnProperty.Type;
import com.butent.bee.egg.shared.Assert;

import java.util.HashMap;
import java.util.Map;

public class ColumnPropertyManager {
  private Map<Type<?>, ColumnProperty> properties = new HashMap<Type<?>, ColumnProperty>();

  public <P extends ColumnProperty> P getColumnProperty(ColumnProperty.Type<P> type) {
    return getColumnProperty(type, true);
  }

  @SuppressWarnings("unchecked")
  public <P extends ColumnProperty> P getColumnProperty(
      ColumnProperty.Type<P> type, boolean useDefault) {
    Object property = properties.get(type);
    if (property == null && useDefault) {
      return type.getDefault();
    }
    return (P) property;
  }

  @SuppressWarnings("unchecked")
  public <P extends ColumnProperty> P removeColumnProperty(ColumnProperty.Type<P> type) {
    Object property = properties.remove(type);
    if (property == null) {
      return null;
    }
    return (P) property;
  }

  public <P extends ColumnProperty> void setColumnProperty(
      ColumnProperty.Type<P> type, P property) {
    Assert.notNull(type, "Cannot add a property with a null type");
    Assert.notNull(property, "Cannot add a null property");
    
    properties.put(type, property);
  }
}
