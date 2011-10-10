package com.butent.bee.server.data;

import com.google.common.collect.HashMultimap;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Maps;
import com.google.common.collect.Multimap;
import com.google.common.primitives.Longs;

import com.butent.bee.server.i18n.I18nUtils;
import com.butent.bee.server.i18n.Localized;
import com.butent.bee.server.sql.SqlBuilderFactory;
import com.butent.bee.server.sql.SqlSelect;
import com.butent.bee.server.sql.SqlUpdate;
import com.butent.bee.server.sql.SqlUtils;
import com.butent.bee.shared.Assert;
import com.butent.bee.shared.communication.ResponseObject;
import com.butent.bee.shared.data.UserData;
import com.butent.bee.shared.i18n.LocalizableConstants;
import com.butent.bee.shared.i18n.LocalizableMessages;
import com.butent.bee.shared.utils.BeeUtils;
import com.butent.bee.shared.utils.LogUtils;

import java.io.IOException;
import java.io.StringReader;
import java.security.Principal;
import java.util.Locale;
import java.util.Map;
import java.util.Properties;
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
    private boolean online = false;
    private Locale locale = Localized.defaultLocale;

    public UserInfo(UserData userData) {
      this.userData = userData;
    }

    public Locale getLocale() {
      return locale;
    }

    public UserData getUserData() {
      return userData;
    }

    public boolean isOnline() {
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
  }

  private static Logger logger = Logger.getLogger(UserServiceBean.class.getName());

  public static final String TBL_USERS = "Users";
  public static final String TBL_ROLES = "Roles";
  public static final String TBL_USER_ROLES = "UserRoles";
  public static final String TBL_PERSONS = "Persons";

  public static final String FLD_LOGIN = "Login";
  public static final String FLD_PASSWORD = "Password";
  public static final String FLD_FIRST_NAME = "FirstName";
  public static final String FLD_LAST_NAME = "LastName";
  public static final String FLD_PROPERTIES = "Properties";
  public static final String FLD_ROLE_NAME = "Name";
  public static final String FLD_USER = "User";
  public static final String FLD_ROLE = "Role";
  public static final String FLD_PERSON = "Person";

  @Resource
  EJBContext ctx;
  @EJB
  SystemBean sys;
  @EJB
  QueryServiceBean qs;

  private final Map<Long, String> roleCache = Maps.newHashMap();
  private final Map<Long, String> userCache = Maps.newHashMap();
  private Map<String, UserInfo> infoCache = Maps.newHashMap();

  private boolean cacheUpToDate = false;

  public String getCurrentUser() {
    Principal p = ctx.getCallerPrincipal();
    Assert.notEmpty(p);
    return p.getName().toLowerCase();
  }

  public long getCurrentUserId() {
    return getUserId(getCurrentUser());
  }

  public Map<Long, String> getRoles() {
    initUsers();
    return ImmutableMap.copyOf(roleCache);
  }

  public String getUser(long userId) {
    Map<Long, String> users = getUsers();
    Assert.contains(users, userId);
    return users.get(userId);
  }

  public long getUserId(String user) {
    Map<Long, String> users = getUsers();

    for (long userId : users.keySet()) {
      if (BeeUtils.same(user, users.get(userId))) {
        return userId;
      }
    }
    Assert.untouchable();
    return 0;
  }

  public long[] getUserRoles(long userId) {
    return Longs.toArray(getUserInfo(userId).getUserData().getRoles());
  }

  public Map<Long, String> getUsers() {
    initUsers();
    return ImmutableMap.copyOf(userCache);
  }

  @Lock(LockType.WRITE)
  public void invalidateCache() {
    cacheUpToDate = false;
  }

  public boolean isUser(String user) {
    for (String usr : getUsers().values()) {
      if (BeeUtils.same(user, usr)) {
        return true;
      }
    }
    return false;
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
  public ResponseObject login(String locale) {
    ResponseObject response = new ResponseObject();
    String user = getCurrentUser();

    if (isUser(user)) {
      UserInfo info = getUserInfo(user);
      info.setOnline(true);
      info.setLocale(locale);

      UserData data = info.getUserData();
      data.setProperty("dsn", SqlBuilderFactory.getDsn());

      qs.updateData(new SqlUpdate(TBL_USERS, "u")
          .addConstant("LastLogin", System.currentTimeMillis())
          .setWhere(SqlUtils.equal("u", sys.getIdName(TBL_USERS), getUserId(user))));

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
      response.addError("Login attempt by an unauthorized user:", user);
      LogUtils.severe(logger, (Object[]) response.getErrors());
    }
    return response;
  }

  @Lock(LockType.WRITE)
  public void logout(String user) {
    if (isUser(user)) {
      UserInfo info = getUserInfo(user);
      String sign = user + " " + BeeUtils.parenthesize(info.getUserData().getUserSign());

      if (info.isOnline()) {
        info.setOnline(false);
        LogUtils.infoNow(logger, "User logged out:", sign);
      } else {
        LogUtils.warning(logger, "User was not logged in:", sign);
      }
    } else if (BeeUtils.isEmpty(getUsers())) {
      LogUtils.warning(logger, "Anonymous user logged out:", user);

    } else {
      LogUtils.severe(logger, "Logout attempt by an unauthorized user:", user);
    }
  }

  @SuppressWarnings("unused")
  @PreDestroy
  @Lock(LockType.WRITE)
  private void destroy() {
    for (String user : getUsers().values()) {
      UserInfo info = getUserInfo(user);

      if (info.isOnline()) {
        logout(info.getUserData().getLogin());
      }
    }
  }

  private UserInfo getCurrentUserInfo() {
    return getUserInfo(getCurrentUserId());
  }

  private UserInfo getUserInfo(long userId) {
    return infoCache.get(getUser(userId));
  }

  private UserInfo getUserInfo(String user) {
    return getUserInfo(getUserId(user));
  }

  @Lock(LockType.WRITE)
  private void initUsers() {
    if (cacheUpToDate) {
      return;
    }
    roleCache.clear();
    userCache.clear();
    Map<String, UserInfo> expiredCache = infoCache;
    infoCache = Maps.newHashMap();

    String userIdName = sys.getIdName(TBL_USERS);
    String roleIdName = sys.getIdName(TBL_ROLES);
    String personIdName = sys.getIdName(TBL_PERSONS);

    SqlSelect ss = new SqlSelect()
        .addFields("r", roleIdName, FLD_ROLE_NAME)
        .addFrom(TBL_ROLES, "r");

    for (Map<String, String> row : qs.getData(ss)) {
      roleCache.put(BeeUtils.toLong(row.get(roleIdName)), row.get(FLD_ROLE_NAME));
    }

    ss = new SqlSelect()
        .addFields("r", FLD_USER, FLD_ROLE)
        .addFrom(TBL_USER_ROLES, "r");

    Multimap<Long, Long> userRoles = HashMultimap.create();

    for (Map<String, String> row : qs.getData(ss)) {
      userRoles.put(BeeUtils.toLong(row.get(FLD_USER)), BeeUtils.toLong(row.get(FLD_ROLE)));
    }

    ss = new SqlSelect()
        .addFields("u", userIdName, FLD_LOGIN, FLD_PROPERTIES)
        .addFields("cc", FLD_FIRST_NAME, FLD_LAST_NAME)
        .addFrom(TBL_USERS, "u").addFromLeft(TBL_PERSONS, "cc",
            SqlUtils.join("u", FLD_PERSON, "cc", personIdName));

    for (Map<String, String> row : qs.getData(ss)) {
      long userId = BeeUtils.toLong(row.get(userIdName));
      String login = row.get(FLD_LOGIN).toLowerCase();

      userCache.put(userId, login);

      UserInfo user = new UserInfo(
          new UserData(userId, login, row.get(FLD_FIRST_NAME), row.get(FLD_LAST_NAME))
              .setRoles(userRoles.get(userId)))
          .setProperties(row.get(FLD_PROPERTIES));

      UserInfo oldInfo = expiredCache.get(login);

      if (!BeeUtils.isEmpty(oldInfo)) {
        user.setLocale(oldInfo.getUserData().getLocale())
            .setOnline(oldInfo.isOnline());
      }
      infoCache.put(login, user);
    }
    cacheUpToDate = true;
  }
}
