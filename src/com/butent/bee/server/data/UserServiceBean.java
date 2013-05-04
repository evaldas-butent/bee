package com.butent.bee.server.data;

import com.google.common.collect.BiMap;
import com.google.common.collect.HashBiMap;
import com.google.common.collect.HashMultimap;
import com.google.common.collect.Maps;
import com.google.common.collect.Multimap;
import com.google.common.primitives.Longs;

import static com.butent.bee.shared.modules.commons.CommonsConstants.*;

import com.butent.bee.server.i18n.I18nUtils;
import com.butent.bee.server.i18n.Localizations;
import com.butent.bee.server.sql.HasConditions;
import com.butent.bee.server.sql.IsCondition;
import com.butent.bee.server.sql.SqlBuilderFactory;
import com.butent.bee.server.sql.SqlSelect;
import com.butent.bee.server.sql.SqlUpdate;
import com.butent.bee.server.sql.SqlUtils;
import com.butent.bee.shared.Assert;
import com.butent.bee.shared.communication.ResponseObject;
import com.butent.bee.shared.data.SimpleRowSet;
import com.butent.bee.shared.data.SimpleRowSet.SimpleRow;
import com.butent.bee.shared.data.UserData;
import com.butent.bee.shared.i18n.LocalizableConstants;
import com.butent.bee.shared.i18n.LocalizableMessages;
import com.butent.bee.shared.logging.BeeLogger;
import com.butent.bee.shared.logging.LogUtils;
import com.butent.bee.shared.modules.commons.CommonsConstants.RightsObjectType;
import com.butent.bee.shared.modules.commons.CommonsConstants.RightsState;
import com.butent.bee.shared.utils.BeeUtils;
import com.butent.bee.shared.utils.NameUtils;

import java.io.IOException;
import java.io.StringReader;
import java.security.Principal;
import java.util.Collection;
import java.util.Locale;
import java.util.Map;
import java.util.Properties;
import java.util.Set;

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
    private final UserData userData;
    private Collection<Long> userRoles;
    private boolean online = false;
    private Locale locale = Localizations.defaultLocale;

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

        if (loc == null) {
          logger.warning(data.getUserSign(), "Unknown user locale:", locale);
        }
      }
      if (loc == null) {
        loc = Localizations.defaultLocale;
      }
      data.setLocale(loc.toString());

      data.setConstants(Localizations.getDictionary(loc));

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
          logger.error(e, properties);
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

  private static BeeLogger logger = LogUtils.getLogger(UserServiceBean.class);

  @Resource
  EJBContext ctx;
  @EJB
  SystemBean sys;
  @EJB
  QueryServiceBean qs;

  private final Map<Long, String> roleCache = Maps.newHashMap();
  private final BiMap<Long, String> userCache = HashBiMap.create();
  private Map<String, UserInfo> infoCache = Maps.newHashMap();
  private final Map<RightsObjectType, Map<String, Multimap<RightsState, Long>>> rightsCache = Maps
      .newHashMap();

  public String getCurrentUser() {
    Principal p = ctx.getCallerPrincipal();
    Assert.notNull(p);
    return p.getName().toLowerCase();
  }

  public Long getCurrentUserId() {
    return getUserId(getCurrentUser());
  }

  public LocalizableConstants getLocalizableConstants() {
    return Localizations.getConstants(getCurrentUserInfo().getLocale());
  }

  public LocalizableMessages getLocalizableMesssages() {
    return Localizations.getMessages(getCurrentUserInfo().getLocale());
  }

  public String getRoleName(long roleId) {
    Assert.contains(roleCache, roleId);
    return roleCache.get(roleId);
  }

  public Set<Long> getRoles() {
    return roleCache.keySet();
  }

  public Long getUserId(String user) {
    if (!userCache.inverse().containsKey(key(user))) {
      return null;
    }
    return userCache.inverse().get(key(user));
  }

  public String getUserName(long userId) {
    return userCache.get(userId);
  }

  public long[] getUserRoles(long userId) {
    UserInfo userInfo = getUserInfo(userId);

    if (userInfo != null) {
      return Longs.toArray(getUserInfo(userId).getRoles());
    }
    return new long[0];
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
        .addFields(TBL_OBJECTS, COL_OBJECT_NAME)
        .addFields(TBL_RIGHTS, COL_ROLE, COL_STATE)
        .addFrom(TBL_OBJECTS)
        .addFromInner(TBL_RIGHTS, sys.joinTables(TBL_OBJECTS, TBL_RIGHTS, COL_OBJECT));

    for (RightsObjectType tp : RightsObjectType.values()) {
      if (BeeUtils.isEmpty(tp.getRegisteredStates())) {
        continue;
      }
      HasConditions cl = SqlUtils.or();
      IsCondition wh =
          SqlUtils.and(SqlUtils.equals(TBL_OBJECTS, COL_OBJECT_TYPE, tp.ordinal()), cl);

      for (RightsState state : tp.getRegisteredStates()) {
        cl.add(SqlUtils.equals(TBL_RIGHTS, COL_STATE, state.ordinal()));
      }
      SimpleRowSet res = qs.getData(ss.setWhere(wh));

      if (res.getNumberOfRows() > 0) {
        Map<String, Multimap<RightsState, Long>> rightsObjects = rightsCache.get(tp);

        if (rightsObjects == null) {
          rightsObjects = Maps.newHashMap();
          rightsCache.put(tp, rightsObjects);
        }
        for (int i = 0; i < res.getNumberOfRows(); i++) {
          RightsState state = NameUtils.getEnumByIndex(RightsState.class, res.getInt(i, COL_STATE));
          String objectName = BeeUtils.normalize(res.getValue(i, COL_OBJECT_NAME));
          Multimap<RightsState, Long> objectStates = rightsObjects.get(objectName);

          if (objectStates == null) {
            objectStates = HashMultimap.create();
            rightsObjects.put(objectName, objectStates);
          }
          objectStates.put(state, res.getLong(i, COL_ROLE));
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
        .addFields(TBL_ROLES, roleIdName, COL_ROLE_NAME)
        .addFrom(TBL_ROLES));

    for (int i = 0; i < rs.getNumberOfRows(); i++) {
      roleCache.put(rs.getLong(i, roleIdName), rs.getValue(i, COL_ROLE_NAME));
    }

    rs = qs.getData(new SqlSelect()
        .addFields(TBL_USER_ROLES, COL_USER, COL_ROLE)
        .addFrom(TBL_USER_ROLES));

    Multimap<Long, Long> userRoles = HashMultimap.create();

    for (int i = 0; i < rs.getNumberOfRows(); i++) {
      userRoles.put(rs.getLong(i, COL_USER), rs.getLong(i, COL_ROLE));
    }

    SqlSelect ss = new SqlSelect()
        .addFields(TBL_USERS, userIdName, COL_LOGIN, UserData.FLD_COMPANY_PERSON, COL_PROPERTIES)
        .addFields(TBL_COMPANY_PERSONS, COL_COMPANY)
        .addFields(TBL_PERSONS, UserData.FLD_FIRST_NAME, UserData.FLD_LAST_NAME)
        .addFrom(TBL_USERS)
        .addFromLeft(TBL_COMPANY_PERSONS,
            sys.joinTables(TBL_COMPANY_PERSONS, TBL_USERS, COL_COMPANY_PERSON))
        .addFromLeft(TBL_PERSONS, sys.joinTables(TBL_PERSONS, TBL_COMPANY_PERSONS, COL_PERSON));

    for (SimpleRow row : qs.getData(ss)) {
      long userId = row.getLong(userIdName);
      String login = key(row.getValue(COL_LOGIN));

      userCache.put(userId, login);

      UserData userData = new UserData(userId, login, row.getValue(UserData.FLD_FIRST_NAME),
          row.getValue(UserData.FLD_LAST_NAME), row.getLong(UserData.FLD_COMPANY_PERSON),
          row.getLong(COL_COMPANY));

      UserInfo user = new UserInfo(userData)
          .setRoles(userRoles.get(userId))
          .setProperties(row.getValue(COL_PROPERTIES));

      UserInfo oldInfo = expiredCache.get(login);

      if (oldInfo != null) {
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

  @Lock(LockType.WRITE)
  public ResponseObject login(String locale, String host, String agent) {
    ResponseObject response = new ResponseObject();
    String user = getCurrentUser();

    if (isUser(user)) {
      UserInfo info = getUserInfo(getUserId(user));
      info.setOnline(true);
      info.setLocale(locale);

      UserData data = info.getUserData();
      data.setProperty("dsn", SqlBuilderFactory.getDsn())
          .setRights(getUserRights(getUserId(user)));

      qs.updateData(new SqlUpdate(TBL_USERS)
          .addConstant(COL_HOST, BeeUtils.joinWords(host, agent))
          .setWhere(sys.idEquals(TBL_USERS, getUserId(user))));

      response.setResponse(data).addInfo("User logged in:",
          user + " " + BeeUtils.parenthesize(data.getUserSign()));

    } else if (BeeUtils.isEmpty(getUsers())) {
      response.setResponse(
          new UserData(-1, user, null, null, null, null)
              .setProperty("dsn", SqlBuilderFactory.getDsn()))
          .addWarning("Anonymous user logged in:", user);

    } else {
      response.addError("Login attempt by an unauthorized user:", user);
    }
    response.log(logger);

    return response;
  }

  @Lock(LockType.WRITE)
  public void logout(String user) {
    if (isUser(user)) {
      UserInfo info = getUserInfo(getUserId(user));
      String sign = user + " " + BeeUtils.parenthesize(info.getUserData().getUserSign());

      qs.updateData(new SqlUpdate(TBL_USERS)
          .addConstant(COL_HOST, null)
          .setWhere(sys.idEquals(TBL_USERS, getUserId(user))));

      if (info.isOnline()) {
        info.setOnline(false);
        logger.info("User logged out:", sign);
      } else {
        logger.warning("User was not logged in:", sign);
      }
    } else if (BeeUtils.isEmpty(getUsers())) {
      logger.warning("Anonymous user logged out:", user);

    } else {
      logger.severe("Logout attempt by an unauthorized user:", user);
    }
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
