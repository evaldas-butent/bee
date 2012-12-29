package com.butent.bee.shared.modules;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.common.collect.Sets;

import com.butent.bee.shared.Assert;
import com.butent.bee.shared.BeeSerializable;
import com.butent.bee.shared.data.DataUtils;
import com.butent.bee.shared.time.DateTime;
import com.butent.bee.shared.time.JustDate;
import com.butent.bee.shared.time.TimeUtils;
import com.butent.bee.shared.utils.BeeUtils;
import com.butent.bee.shared.utils.Codec;
import com.butent.bee.shared.utils.NameUtils;

import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class BeeParameter implements BeeSerializable {

  public static BeeParameter restore(String s) {
    BeeParameter parameter = new BeeParameter();
    parameter.deserialize(s);
    return parameter;
  }

  private String module;
  private String name;
  private ParameterType type;
  private String description;
  private String value;
  private Map<Long, String> userValues = null;

  private enum Serial {
    MODULE, NAME, TYPE, DESCRIPTION, VALUE, USER_VALUES;
  }

  public BeeParameter(String module, String name, ParameterType type, String description,
      boolean userMode, Object defValue) {
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
          setType(NameUtils.getEnumByName(ParameterType.class, val));
          break;
        case DESCRIPTION:
          setDescription(val);
          break;
        case VALUE:
          setValue(val);
          break;
        case USER_VALUES:
          String[] pairs = Codec.beeDeserializeCollection(val);

          if (pairs != null) {
            setUserMode(true);

            for (int j = 0; j < pairs.length; j += 2) {
              setUserValue(BeeUtils.toLong(pairs[j]), pairs[j + 1]);
            }
          }
          break;
      }
    }
  }

  public Boolean getBoolean() {
    return (Boolean) getTypedValue(ParameterType.BOOLEAN, getValue());
  }

  public Boolean getBoolean(Long userId) {
    return (Boolean) getTypedValue(ParameterType.BOOLEAN, getUserValue(userId));
  }

  public JustDate getDate() {
    return (JustDate) getTypedValue(ParameterType.DATE, getValue());
  }

  public JustDate getDate(Long userId) {
    return (JustDate) getTypedValue(ParameterType.DATE, getUserValue(userId));
  }

  public DateTime getDateTime() {
    return (DateTime) getTypedValue(ParameterType.DATETIME, getValue());
  }

  public DateTime getDateTime(Long userId) {
    return (DateTime) getTypedValue(ParameterType.DATETIME, getUserValue(userId));
  }

  public String getDescription() {
    return description;
  }

  @SuppressWarnings("unchecked")
  public List<String> getList() {
    return (List<String>) getTypedValue(ParameterType.LIST, getValue());
  }

  @SuppressWarnings("unchecked")
  public List<String> getList(Long userId) {
    return (List<String>) getTypedValue(ParameterType.LIST, getUserValue(userId));
  }

  @SuppressWarnings("unchecked")
  public Map<String, String> getMap() {
    return (Map<String, String>) getTypedValue(ParameterType.MAP, getValue());
  }

  @SuppressWarnings("unchecked")
  public Map<String, String> getMap(Long userId) {
    return (Map<String, String>) getTypedValue(ParameterType.MAP, getUserValue(userId));
  }

  public String getModule() {
    return module;
  }

  public String getName() {
    return name;
  }

  public Number getNumber() {
    return (Number) getTypedValue(ParameterType.NUMBER, getValue());
  }

  public Number getNumber(Long userId) {
    return (Number) getTypedValue(ParameterType.NUMBER, getUserValue(userId));
  }

  @SuppressWarnings("unchecked")
  public Set<String> getSet() {
    return (Set<String>) getTypedValue(ParameterType.SET, getValue());
  }

  @SuppressWarnings("unchecked")
  public Set<String> getSet(Long userId) {
    return (Set<String>) getTypedValue(ParameterType.SET, getUserValue(userId));
  }

  public String getText() {
    return (String) getTypedValue(ParameterType.TEXT, getValue());
  }

  public String getText(Long userId) {
    return (String) getTypedValue(ParameterType.TEXT, getUserValue(userId));
  }

  public Integer getTime() {
    return (Integer) getTypedValue(ParameterType.TIME, getValue());
  }

  public Integer getTime(Long userId) {
    return (Integer) getTypedValue(ParameterType.TIME, getUserValue(userId));
  }

  public ParameterType getType() {
    return type;
  }

  public String getUserValue(Long userId) {
    Assert.state(supportsUsers(), "Parameter does not support user values: "
        + BeeUtils.join(".", getModule(), getName()));
    Assert.isTrue(DataUtils.isId(userId));

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
          arr[i++] = getModule();
          break;
        case NAME:
          arr[i++] = getName();
          break;
        case TYPE:
          arr[i++] = getType();
          break;
        case DESCRIPTION:
          arr[i++] = getDescription();
          break;
        case VALUE:
          arr[i++] = getValue();
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

  public void setType(ParameterType type) {
    Assert.notNull(type);

    if (type != this.type) {
      this.type = type;
      this.value = null;
    }
  }

  public void setUserMode(boolean userMode) {
    if (userMode) {
      this.userValues = Maps.newHashMap();
    } else {
      this.userValues = null;
    }
  }

  public void setUserValue(Long userId, Object value) {
    Assert.state(supportsUsers(), "Parameter does not support user values: "
        + BeeUtils.join(".", getModule(), getName()));
    Assert.isTrue(DataUtils.isId(userId));

    if (value == null) {
      userValues.remove(userId);
    } else {
      userValues.put(userId, transform(value));
    }
  }

  public void setValue(Object value) {
    this.value = transform(value);
  }

  public boolean supportsUsers() {
    return (userValues != null);
  }

  private Object getTypedValue(ParameterType tp, String expr) {
    Assert.state(getType() == tp,
        BeeUtils.joinWords("Parameter type mismach:", getType(), "!=", tp));
    Object val = null;

    switch (tp) {
      case BOOLEAN:
        val = BeeUtils.toBooleanOrNull(expr);
        break;

      case DATE:
        val = TimeUtils.parseDate(expr);
        break;

      case DATETIME:
        val = TimeUtils.parseDateTime(expr);
        break;

      case LIST:
        String[] entries = Codec.beeDeserializeCollection(expr);

        if (entries != null) {
          List<String> lst = Lists.newArrayListWithCapacity(entries.length);

          for (String entry : entries) {
            lst.add(entry);
          }
          val = lst;
        }
        break;

      case MAP:
        entries = Codec.beeDeserializeCollection(expr);

        if (entries != null) {
          Map<String, String> map = Maps.newHashMapWithExpectedSize(entries.length / 2);

          for (int i = 0; i < entries.length; i += 2) {
            map.put(entries[i], entries[i + 1]);
          }
          val = map;
        }
        break;

      case NUMBER:
        val = BeeUtils.toDoubleOrNull(expr);
        break;

      case SET:
        entries = Codec.beeDeserializeCollection(expr);

        if (entries != null) {
          Set<String> set = Sets.newHashSetWithExpectedSize(entries.length);

          for (String entry : entries) {
            set.add(entry);
          }
          val = set;
        }
        break;

      case TEXT:
        val = expr;
        break;

      case TIME:
        val = TimeUtils.parseTime(expr);
        break;
    }
    return val;
  }

  private String transform(Object val) {
    String expr = null;

    if (val != null) {
      if (val instanceof Map || val instanceof Collection) {
        expr = Codec.beeSerialize(val);
      } else {
        expr = val.toString();
      }
    }
    return expr;
  }
}
