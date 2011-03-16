package com.butent.bee.shared.menu;

import com.butent.bee.shared.Assert;
import com.butent.bee.shared.BeeSerializable;
import com.butent.bee.shared.utils.BeeUtils;
import com.butent.bee.shared.utils.Codec;

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

  public void deserialize(String s) {
    Assert.notEmpty(s);
    String[] arr = Codec.deserializeValues(s);
    Assert.lengthEquals(arr, 11);
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

  public String getKeyName() {
    return keyName;
  }

  public int getOrder() {
    return order;
  }

  public String getParameters() {
    return parameters;
  }

  public String getParent() {
    return parent;
  }

  public int getSeparators() {
    return separators;
  }

  public String getService() {
    return service;
  }

  public String getStyle() {
    return style;
  }

  public String getText() {
    return text;
  }

  public String getType() {
    return type;
  }

  public boolean isLeaf() {
    return BeeUtils.same(getType(), "B");
  }

  public boolean isRoot() {
    return BeeUtils.isEmpty(getParent());
  }

  public boolean isValid() {
    return BeeUtils.allNotEmpty(getId(), getText());
  }

  public boolean isVisible() {
    return visible;
  }

  public String serialize() {
    return Codec.serializeValues(id, parent, order, separators, text,
        service, parameters, type, style, keyName, visible);
  }

  public void setId(String id) {
    this.id = id;
  }

  public void setKeyName(String keyName) {
    this.keyName = keyName;
  }

  public void setOrder(int order) {
    this.order = order;
  }

  public void setParameters(String parameters) {
    this.parameters = parameters;
  }

  public void setParent(String parent) {
    this.parent = parent;
  }

  public void setSeparators(int separators) {
    this.separators = separators;
  }

  public void setService(String service) {
    this.service = service;
  }

  public void setStyle(String style) {
    this.style = style;
  }

  public void setText(String text) {
    this.text = text;
  }

  public void setType(String type) {
    this.type = type;
  }

  public void setVisible(boolean visible) {
    this.visible = visible;
  }

}
