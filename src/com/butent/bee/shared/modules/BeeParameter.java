package com.butent.bee.shared.modules;

import com.google.common.collect.Maps;

import com.butent.bee.shared.Assert;
import com.butent.bee.shared.BeeSerializable;
import com.butent.bee.shared.utils.BeeUtils;
import com.butent.bee.shared.utils.Codec;

import java.util.Map;

public class BeeParameter implements BeeSerializable {

  public static BeeParameter restore(String s) {
    BeeParameter parameter = new BeeParameter();
    parameter.deserialize(s);
    return parameter;
  }

  private String module;
  private String name;
  private String type;
  private String description;
  private String value;
  private Map<Long, String> userValues = null;

  private enum Serial {
    MODULE, NAME, TYPE, DESCRIPTION, VALUE, USER_VALUES;
  }

  public BeeParameter(String module, String name, String type, String description,
      boolean userMode, String defValue) {
    Assert.notEmpty(module);
    Assert.notEmpty(name);

    this.module = module;
    this.name = name;
    setType(type);
    setDescription(description);
    setUserMode(userMode);
    setValue(defValue);
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
        case DESCRIPTION:
          description = val;
          break;
        case VALUE:
          value = val;
          break;
        case USER_VALUES:
          String[] pairs = Codec.beeDeserializeCollection(val);

          if (pairs != null) {
            userValues = Maps.newHashMapWithExpectedSize(pairs.length / 2);

            for (int j = 0; j < pairs.length; j += 2) {
              userValues.put(BeeUtils.toLong(pairs[j]), pairs[j + 1]);
            }
          }
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

  public String getUserValue(Long userId) {
    Assert.state(supportsUsers(), "Parameter does not support user values: "
        + BeeUtils.concat(".", getModule(), getName()));
    Assert.notEmpty(userId);

    if (userValues.containsKey(userId)) {
      return userValues.get(userId);
    } else {
      return getValue();
    }
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
        case DESCRIPTION:
          arr[i++] = description;
          break;
        case VALUE:
          arr[i++] = value;
          break;
        case USER_VALUES:
          arr[i++] = userValues;
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

  public void setUserMode(boolean userMode) {
    if (userMode) {
      this.userValues = Maps.newHashMap();
    } else {
      this.userValues = null;
    }
  }

  public void setUserValue(Long userId, String value) {
    Assert.state(supportsUsers(), "Parameter does not support user values: "
        + BeeUtils.concat(".", getModule(), getName()));
    Assert.notEmpty(userId);

    if (value == null) {
      userValues.remove(userId);
    } else {
      userValues.put(userId, value);
    }
  }

  public void setValue(String value) {
    this.value = value;
  }

  public boolean supportsUsers() {
    return (userValues != null);
  }
}
