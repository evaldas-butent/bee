package com.butent.bee.egg.server.datasource.datatable;

import com.google.common.collect.Maps;

import com.butent.bee.egg.server.datasource.datatable.value.ValueType;

import java.util.Collections;
import java.util.Map;

public class ColumnDescription {

  private String id;
  private ValueType type;
  private String label;
  private String pattern;
  private Map<String, String> customProperties = null;

  public ColumnDescription(String id, ValueType type, String label) {
    this.id = id;
    this.type = type;
    this.label = label;
    this.pattern = "";
  }

  @Override
  public ColumnDescription clone() {
    ColumnDescription result = new ColumnDescription(id, type, label);
    result.setPattern(pattern);
    
    if (customProperties != null) {
      result.customProperties = Maps.newHashMap();
      for (Map.Entry<String, String> entry : customProperties.entrySet()) {
        result.customProperties.put(entry.getKey(), entry.getValue());
      }
    }
    return result;
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

  public String getId() {
    return id;
  }

  public String getLabel() {
    return label;
  }

  public String getPattern() {
    return pattern;
  }

  public ValueType getType() {
    return type;
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

  public void setLabel(String label) {
    this.label = label;
  }

  public void setPattern(String pattern) {
    this.pattern = pattern;
  }
}
