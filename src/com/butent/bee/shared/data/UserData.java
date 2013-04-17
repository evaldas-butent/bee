package com.butent.bee.shared.data;

import com.google.common.collect.HashMultimap;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.common.collect.Multimap;

import com.butent.bee.shared.Assert;
import com.butent.bee.shared.BeeSerializable;
import com.butent.bee.shared.HasInfo;
import com.butent.bee.shared.logging.BeeLogger;
import com.butent.bee.shared.logging.LogUtils;
import com.butent.bee.shared.modules.commons.CommonsConstants.RightsObjectType;
import com.butent.bee.shared.modules.commons.CommonsConstants.RightsState;
import com.butent.bee.shared.utils.ArrayUtils;
import com.butent.bee.shared.utils.BeeUtils;
import com.butent.bee.shared.utils.Codec;
import com.butent.bee.shared.utils.NameUtils;
import com.butent.bee.shared.utils.Property;
import com.butent.bee.shared.utils.PropertyUtils;

import java.util.Collection;
import java.util.List;
import java.util.Map;

/**
 * Contains core user data like login, first and last names, user id etc.
 */

public class UserData implements BeeSerializable, HasInfo {

  /**
   * Contains serializable members of user data (login, first and last names, position etc).
   */

  private enum Serial {
    LOGIN, USER_ID, FIRST_NAME, LAST_NAME, COMPANY_PERSON, LOCALE, PROPERTIES, RIGHTS, CONSTANTS
  }

  public static final String FLD_FIRST_NAME = "FirstName";
  public static final String FLD_LAST_NAME = "LastName";
  public static final String FLD_COMPANY_PERSON = "CompanyPerson";

  private static BeeLogger logger = LogUtils.getLogger(UserData.class);

  public static UserData restore(String s) {
    UserData data = new UserData();
    data.deserialize(s);
    return data;
  }

  private String login;
  private long userId;
  private String firstName;
  private String lastName;
  private Long companyPerson;
  private String locale;
  private Map<String, String> properties;

  private Map<RightsState, Multimap<RightsObjectType, String>> rights;

  private Map<String, String> constants;

  public UserData(long userId, String login, String firstName, String lastName,
      Long companyPerson) {
    this.userId = userId;
    this.login = login;
    this.firstName = firstName;
    this.lastName = lastName;
    this.companyPerson = companyPerson;
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
        case COMPANY_PERSON:
          this.companyPerson = BeeUtils.toLongOrNull(value);
          break;
        case LOCALE:
          this.locale = value;
          break;
        case PROPERTIES:
          String[] entry = Codec.beeDeserializeCollection(value);

          if (!ArrayUtils.isEmpty(entry)) {
            properties = Maps.newHashMap();

            for (int j = 0; j < entry.length; j += 2) {
              properties.put(entry[j], entry[j + 1]);
            }
          }
          break;
        case RIGHTS:
          entry = Codec.beeDeserializeCollection(value);

          if (!ArrayUtils.isEmpty(entry)) {
            rights = Maps.newHashMap();

            for (int j = 0; j < entry.length; j += 2) {
              Multimap<RightsObjectType, String> x = HashMultimap.create();
              String[] oArr = Codec.beeDeserializeCollection(entry[j + 1]);

              for (int k = 0; k < oArr.length; k += 2) {
                RightsObjectType type = NameUtils.getEnumByName(RightsObjectType.class, oArr[k]);
                x.putAll(type, Lists.newArrayList(Codec.beeDeserializeCollection(oArr[k + 1])));
              }
              rights.put(NameUtils.getEnumByName(RightsState.class, entry[j]), x);
            }
          }
          break;
        case CONSTANTS:
          entry = Codec.beeDeserializeCollection(value);

          if (!ArrayUtils.isEmpty(entry)) {
            constants = Maps.newHashMap();
            for (int j = 0; j < entry.length; j += 2) {
              constants.put(entry[j], entry[j + 1]);
            }
          }
          break;
      }
    }
  }

  public Long getCompanyPerson() {
    return companyPerson;
  }

  public String getConstant(String name) {
    return (constants == null) ? null : constants.get(name);
  }

  public Map<String, String> getConstants() {
    return constants;
  }

  public String getFirstName() {
    return firstName;
  }

  @Override
  public List<Property> getInfo() {
    List<Property> info = PropertyUtils.createProperties("Login", getLogin(),
        "User Id", getUserId(),
        "First Name", getFirstName(),
        "Last Name", getLastName(),
        "Company Person ID", getCompanyPerson(),
        "Locale", getLocale());

    if (!BeeUtils.isEmpty(properties)) {
      info.add(new Property("Properties", BeeUtils.bracket(properties.size())));
      info.addAll(PropertyUtils.createProperties(properties));
    }

    if (!BeeUtils.isEmpty(rights)) {
      info.add(new Property("Rights", BeeUtils.bracket(rights.size())));
      for (Map.Entry<RightsState, Multimap<RightsObjectType, String>> entry : rights.entrySet()) {
        info.add(new Property(entry.getKey().toString(), entry.getValue().toString()));
      }
    }

    if (!BeeUtils.isEmpty(constants)) {
      info.add(new Property("Constants", BeeUtils.bracket(constants.size())));
      info.addAll(PropertyUtils.createProperties(constants));
    }

    return info;
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

  public long getUserId() {
    return userId;
  }

  public String getUserSign() {
    return BeeUtils.notEmpty(BeeUtils.joinWords(getFirstName(), getLastName()), getLogin());
  }

  public boolean hasEventRight(String object, RightsState state) {
    return hasRight(RightsObjectType.EVENT, object, state);
  }

  public boolean hasFormRight(String object, RightsState state) {
    return hasRight(RightsObjectType.FORM, object, state);
  }

  public boolean hasGridRight(String object, RightsState state) {
    return hasRight(RightsObjectType.GRID, object, state);
  }

  public boolean hasMenuRight(String object, RightsState state) {
    return hasRight(RightsObjectType.MENU, object, state);
  }

  public boolean hasModuleRight(String object, RightsState state) {
    return hasRight(RightsObjectType.MODULE, object, state);
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
        case COMPANY_PERSON:
          arr[i++] = companyPerson;
          break;
        case LOCALE:
          arr[i++] = locale;
          break;
        case PROPERTIES:
          arr[i++] = properties;
          break;
        case RIGHTS:
          Map<RightsState, Map<RightsObjectType, Collection<String>>> x = null;

          if (!BeeUtils.isEmpty(rights)) {
            x = Maps.newHashMap();

            for (RightsState state : rights.keySet()) {
              x.put(state, rights.get(state).asMap());
            }
          }
          arr[i++] = x;
          break;
        case CONSTANTS:
          arr[i++] = constants;
          break;
      }
    }
    return Codec.beeSerialize(arr);
  }

  public void setConstants(Map<String, String> constants) {
    this.constants = constants;
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

  public void setRights(Map<RightsState, Multimap<RightsObjectType, String>> userRights) {
    rights = userRights;
  }

  private boolean hasRight(RightsObjectType type, String object, RightsState state) {
    Assert.notNull(state);

    if (!BeeUtils.contains(type.getRegisteredStates(), state)) {
      logger.severe("State", BeeUtils.bracket(state.name()),
          "is not registered for type", BeeUtils.bracket(type.name()));
      return false;
    }
    if (BeeUtils.isEmpty(object)) {
      return true;
    }
    boolean checked = state.isChecked();

    if (!BeeUtils.isEmpty(rights)) {
      Multimap<RightsObjectType, String> stateObjects = rights.get(state);

      if (stateObjects.containsKey(type)) {
        checked = (stateObjects.get(type).contains(BeeUtils.normalize(object)) != checked);
      }
    }
    return checked;
  }
}
