package com.butent.bee.shared.data;

import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;

import com.butent.bee.shared.Assert;
import com.butent.bee.shared.BeeSerializable;
import com.butent.bee.shared.utils.BeeUtils;
import com.butent.bee.shared.utils.Codec;

import java.util.Collection;
import java.util.Map;

/**
 * Contains core user data like login, first and last names, user id etc.
 */

public class UserData implements BeeSerializable {

  /**
   * Contains serializable members of user data (login, first and last names, position etc).
   */

  private enum Serial {
    LOGIN, USER_ID, FIRST_NAME, LAST_NAME, ROLES, LOCALE, PROPERTIES
  }

  public static final String FLD_FIRST_NAME = "FirstName";
  public static final String FLD_LAST_NAME = "LastName";
  
  public static UserData restore(String s) {
    UserData data = new UserData();
    data.deserialize(s);
    return data;
  }

  private String login;
  private long userId;
  private String firstName;
  private String lastName;
  private Collection<Long> userRoles;
  private String locale;
  private Map<String, String> properties;

  public UserData(long userId, String login, String firstName, String lastName) {
    this.userId = userId;
    this.login = login;
    this.firstName = firstName;
    this.lastName = lastName;
  }

  private UserData() {
  }

  @Override
  public void deserialize(String s) {
    String[] arr = Codec.beeDeserializeCollection(s);
    Serial[] members = Serial.values();
    Assert.lengthEquals(arr, members.length);

    for (int i = 0; i < members.length; i++) {
      Serial member = members[i];
      String value = arr[i];

      switch (member) {
        case LOGIN:
          this.login = value;
          break;
        case USER_ID:
          this.userId = BeeUtils.toLong(value);
          break;
        case FIRST_NAME:
          this.firstName = value;
          break;
        case LAST_NAME:
          this.lastName = value;
          break;
        case ROLES:
          String[] roles = Codec.beeDeserializeCollection(value);

          if (!BeeUtils.isEmpty(roles)) {
            userRoles = Lists.newArrayList();

            for (String role : roles) {
              userRoles.add(BeeUtils.toLong(role));
            }
          }
          break;
        case LOCALE:
          this.locale = value;
          break;
        case PROPERTIES:
          String[] props = Codec.beeDeserializeCollection(value);

          if (!BeeUtils.isEmpty(props)) {
            properties = Maps.newHashMap();

            for (int j = 0; j < props.length; j += 2) {
              properties.put(props[j], props[j + 1]);
            }
          }
          break;
      }
    }
  }

  public String getFirstName() {
    return firstName;
  }

  public String getLastName() {
    return lastName;
  }

  public String getLocale() {
    return locale;
  }

  public String getLogin() {
    return login;
  }

  public Map<String, String> getProperties() {
    return ImmutableMap.copyOf(properties);
  }

  public String getProperty(String name) {
    if (properties != null) {
      return this.properties.get(name);
    }
    return null;
  }

  public Collection<Long> getRoles() {
    return userRoles;
  }

  public long getUserId() {
    return userId;
  }

  public String getUserSign() {
    return BeeUtils.ifString(BeeUtils.concat(1, getFirstName(), getLastName()), getLogin());
  }

  @Override
  public String serialize() {
    Serial[] members = Serial.values();
    Object[] arr = new Object[members.length];
    int i = 0;

    for (Serial member : members) {
      switch (member) {
        case LOGIN:
          arr[i++] = login;
          break;
        case USER_ID:
          arr[i++] = userId;
          break;
        case FIRST_NAME:
          arr[i++] = firstName;
          break;
        case LAST_NAME:
          arr[i++] = lastName;
          break;
        case ROLES:
          arr[i++] = userRoles;
          break;
        case LOCALE:
          arr[i++] = locale;
          break;
        case PROPERTIES:
          arr[i++] = properties;
          break;
      }
    }
    return Codec.beeSerialize(arr);
  }

  public UserData setLocale(String locale) {
    this.locale = locale;
    return this;
  }

  public void setProperties(Map<String, String> properties) {
    this.properties = properties;
  }

  public UserData setProperty(String name, String value) {
    if (this.properties == null) {
      this.properties = Maps.newHashMap();
    }
    this.properties.put(name, value);
    return this;
  }

  public UserData setRoles(Collection<Long> userRoles) {
    this.userRoles = userRoles;
    return this;
  }
}
