package com.butent.bee.egg.shared.data;

import com.butent.bee.egg.shared.Assert;
import com.butent.bee.egg.shared.data.value.ValueType;

public class TableColumn implements IsColumn {
  private String id;
  private ValueType type;
  private String label;
  private String pattern;
  private CustomProperties properties = null;

  public TableColumn(String id, ValueType type, String label) {
    this.id = id;
    this.type = type;
    this.label = label;
    this.pattern = "";
  }

  @Override
  public TableColumn clone() {
    TableColumn result = new TableColumn(id, type, label);
    result.setPattern(pattern);
    if (properties != null) {
      result.properties = properties.clone();
    }
    return result;
  }

  public String getId() {
    return id;
  }

  public String getLabel() {
    return label;
  }

  public String getPattern() {
    return pattern;
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
    return type;
  }

  public void setId(String id) {
    this.id = id;
  }

  public void setLabel(String label) {
    this.label = label;
  }

  public void setPattern(String pattern) {
    this.pattern = pattern;
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

  public void setType(ValueType type) {
    this.type = type;
  }
}
