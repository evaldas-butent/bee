package com.butent.bee.egg.shared.menu;

import com.butent.bee.egg.shared.Assert;
import com.butent.bee.egg.shared.BeeSerializable;
import com.butent.bee.egg.shared.utils.BeeUtils;

public class MenuEntry implements BeeSerializable {
  private String id = null;
  private String parent = null;

  private int order = 0;
  private int separators = 0;

  private String text = null;

  private String service = null;
  private String parameters = null;

  private String type = null;
  private String style = null;

  private String keyName = null;

  private boolean visible = false;

  public MenuEntry() {
    super();
  }

  public String getParent() {
    return parent;
  }

  public void setParent(String parent) {
    this.parent = parent;
  }

  public int getOrder() {
    return order;
  }

  public void setOrder(int order) {
    this.order = order;
  }

  public int getSeparators() {
    return separators;
  }

  public void setSeparators(int separators) {
    this.separators = separators;
  }

  public String getText() {
    return text;
  }

  public void setText(String text) {
    this.text = text;
  }

  public String getService() {
    return service;
  }

  public void setService(String service) {
    this.service = service;
  }

  public String getParameters() {
    return parameters;
  }

  public void setParameters(String parameters) {
    this.parameters = parameters;
  }

  public String getType() {
    return type;
  }

  public void setType(String type) {
    this.type = type;
  }

  public String getStyle() {
    return style;
  }

  public void setStyle(String style) {
    this.style = style;
  }

  public String getKeyName() {
    return keyName;
  }

  public void setKeyName(String keyName) {
    this.keyName = keyName;
  }

  public boolean isVisible() {
    return visible;
  }

  public void setVisible(boolean visible) {
    this.visible = visible;
  }

  public String serialize() {
    return BeeUtils.serializeValues(id, parent, order, separators, text,
        service, parameters, type, style, keyName, visible);
  }

  public void deserialize(String s) {
    Assert.notEmpty(s);
    String[] arr = BeeUtils.deserializeValues(s);
    Assert.arrayLength(arr, 11);
    int i = 0;
    
    setId(arr[i++]);
    setParent(arr[i++]);
    setOrder(BeeUtils.toInt(arr[i++]));
    setSeparators(BeeUtils.toInt(arr[i++]));
    setText(arr[i++]);
    setService(arr[i++]);
    setParameters(arr[i++]);
    setType(arr[i++]);
    setStyle(arr[i++]);
    setKeyName(arr[i++]);

    setVisible(BeeUtils.toBoolean(arr[i++]));
  }

  public String getId() {
    return id;
  }

  public void setId(String id) {
    this.id = id;
  }
  
  public boolean isValid() {
    return BeeUtils.allNotEmpty(getId(), getText());
  }
  
  public boolean isRoot() {
    return BeeUtils.isEmpty(getParent());
  }

  public boolean isLeaf() {
    return BeeUtils.same(getType(), "B");
  }
  
}
