package com.butent.bee.shared.data;

import com.butent.bee.shared.Assert;
import com.butent.bee.shared.BeeConst;
import com.butent.bee.shared.HasInfo;
import com.butent.bee.shared.data.value.ValueType;
import com.butent.bee.shared.utils.Property;
import com.butent.bee.shared.utils.PropertyUtils;

import java.util.List;

/**
 * Implements {@code IsColumn} interface, gets and sets table column's properties.
 */

public class TableColumn implements HasInfo, IsColumn {

  private ValueType type;
  
  private String label;
  
  private String id;
  
  private String pattern;
  
  private CustomProperties properties = null;
  
  private int precision = BeeConst.UNDEF;

  private int scale = BeeConst.UNDEF;

  public TableColumn(ValueType type) {
    this(type, null, null);
  }

  public TableColumn(ValueType type, String label, String id) {
    this.type = type;
    this.label = label;
    this.id = id;
    this.pattern = null;
  }

  @Override
  public TableColumn copy() {
    TableColumn result = new TableColumn(type, label, id);
    result.setPattern(pattern);
    if (properties != null) {
      result.properties = properties.copy();
    }
    return result;
  }

  public String getId() {
    return id;
  }

  public List<Property> getInfo() {
    List<Property> lst = PropertyUtils.createProperties("Id", getId(), "Type", getType(),
        "Label", getLabel(), "Pattern", getPattern());
    if (getProperties() != null) {
      lst.addAll(getProperties().getInfo());
    }
    return lst;
  }

  public String getLabel() {
    return label;
  }

  public String getPattern() {
    return pattern;
  }

  public int getPrecision() {
    return precision;
  }

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

  public int getScale() {
    return scale;
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

  public void setPrecision(int precision) {
    this.precision = precision;
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

  public void setScale(int scale) {
    this.scale = scale;
  }
  
  public void setType(ValueType type) {
    this.type = type;
  }
}
