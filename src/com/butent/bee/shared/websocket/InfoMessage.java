package com.butent.bee.shared.websocket;

import com.butent.bee.shared.HasInfo;
import com.butent.bee.shared.utils.Property;
import com.butent.bee.shared.utils.PropertyUtils;

import java.util.List;

public class InfoMessage extends Message implements HasInfo {
  
  private List<Property> info;

  public InfoMessage(List<Property> info) {
    this();
    this.info = info;
  }

  InfoMessage() {
    super(Type.INFO);
  }

  @Override
  public List<Property> getInfo() {
    return info;
  }

  @Override
  protected void deserialize(String s) {
    this.info = PropertyUtils.restoreProperties(s);
  }

  @Override
  protected String serialize() {
    return PropertyUtils.serializeProperties(getInfo());
  }
}
