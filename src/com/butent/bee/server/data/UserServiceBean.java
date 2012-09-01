package com.butent.bee.server.data;

import com.google.common.collect.BiMap;
import com.google.common.collect.HashBiMap;
import com.google.common.collect.HashMultimap;
import com.google.common.collect.Maps;
import com.google.common.collect.Multimap;
import com.google.common.primitives.Longs;

import com.butent.bee.server.i18n.I18nUtils;
import com.butent.bee.server.i18n.Localized;
import com.butent.bee.server.sql.HasConditions;
import com.butent.bee.server.sql.IsCondition;
import com.butent.bee.server.sql.SqlBuilderFactory;
import com.butent.bee.server.sql.SqlInsert;
import com.butent.bee.server.sql.SqlSelect;
import com.butent.bee.server.sql.SqlUtils;
import com.butent.bee.shared.Assert;
import com.butent.bee.shared.communication.ResponseObject;
import com.butent.bee.shared.data.SimpleRowSet;
import com.butent.bee.shared.data.UserData;
import com.butent.bee.shared.i18n.LocalizableConstants;
import com.butent.bee.shared.i18n.LocalizableMessages;
import com.butent.bee.shared.modules.commons.CommonsConstants.RightsObjectType;
import com.butent.bee.shared.modules.commons.CommonsConstants.RightsState;
import com.butent.bee.shared.utils.BeeUtils;
import com.butent.bee.shared.utils.LogUtils;
import com.butent.bee.shared.utils.NameUtils;

import java.io.IOException;
import java.io.StringReader;
import java.security.Principal;
import java.util.Collection;
import java.util.Locale;
import java.util.Map;
import java.util.Properties;
import java.util.Set;
import java.util.logging.Logger;

import javax.annotation.PreDestroy;
import javax.annotation.Resource;
import javax.ejb.EJB;
import javax.ejb.EJBContext;
import javax.ejb.Lock;
import javax.ejb.LockType;
import javax.ejb.Singleton;

/**
 * Responsible for users system, their login status, localization, user and roles cache etc.
 */

@Singleton
@Lock(LockType.READ)
public class UserServiceBean {

  private class UserInfo {
    private UserData userData;
    private Collection<Long> userRoles;
    private boolean online = false;
    private Locale locale = Localized.defaultLocale;

    private UserInfo(UserData userData) {
      this.userData = userData;
    }

    private Locale getLocale() {
      return locale;
    }

    private Collection<Long> getRoles() {
      return userRoles;
    }

    private UserData getUserData() {
      return userData;
    }

    private boolean isOnline() {
      return online;
    }

    private UserInfo setLocale(String locale) {
      Locale loc = null;
      UserData data = getUserData();

      if (!BeeUtils.isEmpty(locale)) {
        loc = I18nUtils.toLocale(locale);

        if (BeeUtils.isEmpty(loc)) {
          LogUtils.warning(logger, data.getUserSign(), "Unknown user locale:", locale);
        }
      }
      if (BeeUtils.isEmpty(loc)) {
        loc = Localized.defaultLocale;
      }
      data.setLocale(loc.toString());
      
      data.setConstants(Localized.getDictionary(loc));
      
      this.locale = loc;
      return this;
    }

    private UserInfo setOnline(boolean online) {
      this.online = online;
      return this;
    }

    private UserInfo setProperties(String properties) {
      Map<String, String> props = null;

      if (!BeeUtils.isEmpty(properties)) {
        props = Maps.newHashMap();
        Properties prp = new Properties();

        try {
          prp.load(new StringReader(properties));
        } catch (IOException e) {
          LogUtils.error(logger, e, properties);
        }
        for (String p : prp.stringPropertyNames()) {
          props.put(p, prp.getProperty(p));
        }
      }
      userData.setProperties(props);
      return this;
    }

    private UserInfo setRoles(Collection<Long> userRoles) {
      this.userRoles = userRoles;
      return this;
    }
  }

  private static Logger logger = Logger.getLogger(UserServiceBean.class.getName());

  public static final String TBL_USERS = "Users";
  public static final String TBL_ROLES = "Roles";
  public static final String TBL_USER_ROLES = "UserRoles";
  public static final String TBL_COMPANY_PERSONS = "CompanyPersons";
  public static final String TBL_PERSONS = "Persons";
  public static final String TBL_USER_HISTORY = "UserHistory";
  public static final String TBL_OBJECTS = "Objects";
  public static final String TBL_RIGHTS = "Rights";

  public static final String FLD_LOGIN = "Login";
  public static final String FLD_PASSWORD = "Password";
  public static final String FLD_PROPERTIES = "Properties";
  public static final String FLD_ROLE_NAME = "Name";
  public static final String FLD_USER = "User";
  public static final String FLD_ROLE = "Role";
  public static final String FLD_COMPANY_PERSON = "CompanyPerson";
  public static final String FLD_PERSON = "Person";
  public static final String FLD_OBJECT_TYPE = "Type";
  public static final String FLD_OBJECT = "Object";
  public static final String FLD_OBJECT_NAME = "Name";
  public static final String FLD_STATE = "State";

  @Resource
  EJBContext ctx;
  @EJB
  SystemBean sys;
  @EJB
  QueryServiceBean qs;

  private final Map<Long, String> roleCache = Maps.newHashMap();
  private final BiMap<Long, String> userCache = HashBiMap.create();
  private Map<String, UserInfo> infoCache = Maps.newHashMap();
  private Map<RightsObjectType, Map<String, Multimap<RightsState, Long>>> rightsCache = Maps
      .newHashMap();

  public String getCurrentUser() {
    Principal p = ctx.getCallerPrincipal();
    Assert.notEmpty(p);
    return p.getName().toLowerCase();
  }

  public long getCurrentUserId() {
    return getUserId(getCurrentUser());
  }

  public String getRoleName(long roleId) {
    Assert.contains(roleCache, roleId);
    return roleCache.get(roleId);
  }

  public Set<Long> getRoles() {
    return roleCache.keySet();
  }

  public long getUserId(String user) {
    Assert.contains(userCache.inverse(), key(user));
    return userCache.inverse().get(key(user));
  }

  public String getUserName(long userId) {
    Assert.contains(userCache, userId);
    return userCache.get(userId);
  }

  public long[] getUserRoles(long userId) {
    return Longs.toArray(getUserInfo(userId).getRoles());
  }

  public Set<Long> getUsers() {
    return userCache.keySet();
  }

  public String getUserSign(long userId) {
    return getUserInfo(userId).getUserData().getUserSign();
  }

  public boolean hasEventRight(String object, RightsState state) {
    return getCurrentUserInfo().getUserData().hasEventRight(object, state);
  }

  public boolean hasFormRight(String object, RightsState state) {
    return getCurrentUserInfo().getUserData().hasFormRight(object, state);
  }

  public boolean hasGridRight(String object, RightsState state) {
    return getCurrentUserInfo().getUserData().hasGridRight(object, state);
  }

  public boolean hasMenuRight(String object, RightsState state) {
    return getCurrentUserInfo().getUserData().hasMenuRight(object, state);
  }

  public boolean hasModuleRight(String object, RightsState state) {
    return getCurrentUserInfo().getUserData().hasModuleRight(object, state);
  }

  @Lock(LockType.WRITE)
  public void initRights() {
    rightsCache.clear();

    SqlSelect ss = new SqlSelect()
        .addFields(TBL_OBJECTS, FLD_OBJECT_NAME)
        .addFields(TBL_RIGHTS, FLD_ROLE, FLD_STATE)
        .addFrom(TBL_OBJECTS)
        .addFromInner(TBL_RIGHTS,
            SqlUtils.join(TBL_OBJECTS, sys.getIdName(TBL_OBJECTS), TBL_RIGHTS, FLD_OBJECT));

    for (RightsObjectType tp : RightsObjectType.values()) {
      if (BeeUtils.isEmpty(tp.getRegisteredStates())) {
        continue;
      }
      HasConditions cl = SqlUtils.or();
      IsCondition wh = SqlUtils.and(SqlUtils.equal(TBL_OBJECTS, FLD_OBJECT_TYPE, tp.ordinal()), cl);

      for (RightsState state : tp.getRegisteredStates()) {
        cl.add(SqlUtils.equal(TBL_RIGHTS, FLD_STATE, state.ordinal()));
      }
      SimpleRowSet res = qs.getData(ss.setWhere(wh));

      if (res.getNumberOfRows() > 0) {
        Map<String, Multimap<RightsState, Long>> rightsObjects = rightsCache.get(tp);

        if (rightsObjects == null) {
          rightsObjects = Maps.newHashMap();
          rightsCache.put(tp, rightsObjects);
        }
        for (int i = 0; i < res.getNumberOfRows(); i++) {
          RightsState state = NameUtils.getEnumByIndex(RightsState.class, res.getInt(i, FLD_STATE));
          String objectName = BeeUtils.normalize(res.getValue(i, FLD_OBJECT_NAME));
          Multimap<RightsState, Long> objectStates = rightsObjects.get(objectName);

          if (objectStates == null) {
            objectStates = HashMultimap.create();
            rightsObjects.put(objectName, objectStates);
          }
          objectStates.put(state, res.getLong(i, FLD_ROLE));
        }
      }
    }
  }

  @Lock(LockType.WRITE)
  public void initUsers() {
    roleCache.clear();
    userCache.clear();
    Map<String, UserInfo> expiredCache = infoCache;
    infoCache = Maps.newHashMap();

    String userIdName = sys.getIdName(TBL_USERS);
    String roleIdName = sys.getIdName(TBL_ROLES);

    SimpleRowSet rs = qs.getData(new SqlSelect()
        .addFields(TBL_ROLES, roleIdName, FLD_ROLE_NAME)
        .addFrom(TBL_ROLES));

    for (int i = 0; i < rs.getNumberOfRows(); i++) {
      roleCache.put(rs.getLong(i, roleIdName), rs.getValue(i, FLD_ROLE_NAME));
    }

    rs = qs.getData(new SqlSelect()
        .addFields(TBL_USER_ROLES, FLD_USER, FLD_ROLE)
        .addFrom(TBL_USER_ROLES));

    Multimap<Long, Long> userRoles = HashMultimap.create();

    for (int i = 0; i < rs.getNumberOfRows(); i++) {
      userRoles.put(rs.getLong(i, FLD_USER), rs.getLong(i, FLD_ROLE));
    }

    SqlSelect ss = new SqlSelect()
        .addFields(TBL_USERS, userIdName, FLD_LOGIN, FLD_PROPERTIES)
        .addFields(TBL_PERSONS, UserData.FLD_FIRST_NAME, UserData.FLD_LAST_NAME)
        .addFrom(TBL_USERS)
        .addFromLeft(TBL_COMPANY_PERSONS,
            SqlUtils.join(TBL_USERS, FLD_COMPANY_PERSON,
                TBL_COMPANY_PERSONS, sys.getIdName(TBL_COMPANY_PERSONS)))
        .addFromLeft(TBL_PERSONS,
            SqlUtils.join(TBL_COMPANY_PERSONS, FLD_PERSON,
                TBL_PERSONS, sys.getIdName(TBL_PERSONS)));

    for (Map<String, String> row : qs.getData(ss)) {
      long userId = BeeUtils.toLong(row.get(userIdName));
      String login = key(row.get(FLD_LOGIN));

      userCache.put(userId, login);

      UserInfo user = new UserInfo(new UserData(userId, login, row.get(UserData.FLD_FIRST_NAME),
          row.get(UserData.FLD_LAST_NAME)))
          .setRoles(userRoles.get(userId))
          .setProperties(row.get(FLD_PROPERTIES));

      UserInfo oldInfo = expiredCache.get(login);

      if (!BeeUtils.isEmpty(oldInfo)) {
        user.setLocale(oldInfo.getUserData().getLocale())
            .setOnline(oldInfo.isOnline());
      }
      infoCache.put(login, user);
    }
  }

  public boolean isRightsTable(String tblName) {
    return BeeUtils.inList(tblName, TBL_OBJECTS, TBL_RIGHTS);
  }

  public boolean isUser(String user) {
    return userCache.inverse().containsKey(key(user));
  }

  public boolean isUserTable(String tblName) {
    return BeeUtils.inList(tblName, TBL_USERS, TBL_ROLES, TBL_USER_ROLES);
  }

  public LocalizableConstants localConstants() {
    return Localized.getConstants(getCurrentUserInfo().getLocale());
  }

  public LocalizableMessages localMesssages() {
    return Localized.getMessages(getCurrentUserInfo().getLocale());
  }

  @Lock(LockType.WRITE)
  public ResponseObject login(String locale, String host, String agent) {
    ResponseObject response = new ResponseObject();
    String user = getCurrentUser();

    SqlInsert si = new SqlInsert(TBL_USER_HISTORY)
        .addConstant("Time", System.currentTimeMillis())
        .addConstant("Name", user)
        .addConstant("Host", host)
        .addConstant("Notes", agent);

    String mode = "IN";

    if (isUser(user)) {
      UserInfo info = getUserInfo(getUserId(user));
      info.setOnline(true);
      info.setLocale(locale);

      UserData data = info.getUserData();
      data.setProperty("dsn", SqlBuilderFactory.getDsn())
          .setRights(getUserRights(getUserId(user)));

      si.addConstant("User", getUserId(user));

      response.setResponse(data).addInfo("User logged in:",
          user + " " + BeeUtils.parenthesize(data.getUserSign()));
      LogUtils.infoNow(logger, (Object[]) response.getNotifications());

    } else if (BeeUtils.isEmpty(getUsers())) {
      response.setResponse(
          new UserData(-1, user, null, null)
              .setProperty("dsn", SqlBuilderFactory.getDsn()))
          .addWarning("Anonymous user logged in:", user);
      LogUtils.warning(logger, (Object[]) response.getWarnings());

    } else {
      mode = "FAIL";
      response.addError("Login attempt by an unauthorized user:", user);
      LogUtils.severe(logger, (Object[]) response.getErrors());
    }
    qs.insertData(si.addConstant("Mode", mode));
    return response;
  }

  @Lock(LockType.WRITE)
  public void logout(String user) {
    SqlInsert si = new SqlInsert(TBL_USER_HISTORY)
        .addConstant("Time", System.currentTimeMillis())
        .addConstant("Name", user);

    String mode = "OUT";

    if (isUser(user)) {
      UserInfo info = getUserInfo(getUserId(user));
      String sign = user + " " + BeeUtils.parenthesize(info.getUserData().getUserSign());

      si.addConstant("User", getUserId(user));

      if (info.isOnline()) {
        info.setOnline(false);
        LogUtils.infoNow(logger, "User logged out:", sign);
      } else {
        LogUtils.warning(logger, "User was not logged in:", sign);
      }
    } else if (BeeUtils.isEmpty(getUsers())) {
      LogUtils.warning(logger, "Anonymous user logged out:", user);

    } else {
      mode = "FAIL";
      LogUtils.severe(logger, "Logout attempt by an unauthorized user:", user);
    }
    qs.insertData(si.addConstant("Mode", mode));
  }

  @PreDestroy
  private void destroy() {
    for (long userId : getUsers()) {
      UserInfo info = getUserInfo(userId);

      if (info.isOnline()) {
        logout(info.getUserData().getLogin());
      }
    }
  }

  private UserInfo getCurrentUserInfo() {
    return getUserInfo(getCurrentUserId());
  }

  private UserInfo getUserInfo(long userId) {
    return infoCache.get(getUserName(userId));
  }

  private Map<RightsState, Multimap<RightsObjectType, String>> getUserRights(long userId) {
    Map<RightsState, Multimap<RightsObjectType, String>> rights = Maps.newHashMap();

    for (RightsState state : RightsState.values()) {
      Multimap<RightsObjectType, String> members = HashMultimap.create();
      rights.put(state, members);

      for (RightsObjectType type : rightsCache.keySet()) {
        Set<String> objects = rightsCache.get(type).keySet();

        for (String object : objects) {
          if (hasRight(userId, type, object, state) != state.isChecked()) {
            members.put(type, object);
          }
        }
      }
    }
    return rights;
  }

  private boolean hasRight(long userId, RightsObjectType type, String object, RightsState state) {
    Assert.notEmpty(object);

    if (state == null) {
      return false;
    }
    boolean checked = state.isChecked();

    Map<String, Multimap<RightsState, Long>> rightsObjects = rightsCache.get(type);

    if (rightsObjects != null) {
      Multimap<RightsState, Long> objectStates = rightsObjects.get(BeeUtils.normalize(object));

      if (objectStates != null && objectStates.containsKey(state)) {
        Collection<Long> roles = objectStates.get(state);
        boolean ok = checked;

        if (roles.contains(null)) {
          ok = !checked;
        } else {
          for (long role : getUserRoles(userId)) {
            ok = (checked != roles.contains(role));

            if (ok) {
              break;
            }
          }
        }
        checked = ok;
      }
    }
    return checked;
  }

  private String key(String value) {
    return value.toLowerCase();
  }
}
