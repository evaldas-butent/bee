package com.butent.bee.shared.modules;

import com.butent.bee.shared.Assert;
import com.butent.bee.shared.BeeSerializable;
import com.butent.bee.shared.utils.Codec;

public class BeeParameter implements BeeSerializable {

  public static BeeParameter restore(String s) {
    BeeParameter parameter = new BeeParameter();
    parameter.deserialize(s);
    return parameter;
  }

  private String module;
  private String name;
  private String type;
  private String value;
  private String description;

  private enum Serial {
    MODULE, NAME, TYPE, VALUE, DESCRIPTION;
  }

  public BeeParameter(String module, String name, String type, String value, String description) {
    Assert.notEmpty(module);
    Assert.notEmpty(name);

    this.module = module;
    this.name = name;
    setType(type);
    setValue(value);
    setDescription(description);
  }

  private BeeParameter() {
  }

  @Override
  public void deserialize(String s) {
    String[] arr = Codec.beeDeserializeCollection(s);
    Serial[] members = Serial.values();
    Assert.lengthEquals(arr, members.length);

    for (int i = 0; i < members.length; i++) {
      Serial member = members[i];
      String val = arr[i];

      switch (member) {
        case MODULE:
          module = val;
          break;
        case NAME:
          name = val;
          break;
        case TYPE:
          type = val;
          break;
        case VALUE:
          value = val;
          break;
        case DESCRIPTION:
          description = val;
          break;
      }
    }
  }

  public String getDescription() {
    return description;
  }

  public String getModule() {
    return module;
  }

  public String getName() {
    return name;
  }

  public String getType() {
    return type;
  }

  public String getValue() {
    return value;
  }

  @Override
  public String serialize() {
    Serial[] members = Serial.values();
    Object[] arr = new Object[members.length];
    int i = 0;

    for (Serial member : Serial.values()) {
      switch (member) {
        case MODULE:
          arr[i++] = module;
          break;
        case NAME:
          arr[i++] = name;
          break;
        case TYPE:
          arr[i++] = type;
          break;
        case VALUE:
          arr[i++] = value;
          break;
        case DESCRIPTION:
          arr[i++] = description;
          break;
      }
    }
    return Codec.beeSerialize(arr);
  }

  public void setDescription(String description) {
    this.description = description;
  }

  public void setType(String type) {
    this.type = type;
  }

  public void setValue(String value) {
    this.value = value;
  }
}
