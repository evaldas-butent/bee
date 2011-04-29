package com.butent.bee.server.data;

import com.google.common.collect.HashMultimap;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Maps;
import com.google.common.collect.Multimap;
import com.google.common.primitives.Longs;

import com.butent.bee.server.i18n.I18nUtils;
import com.butent.bee.server.i18n.Localized;
import com.butent.bee.server.sql.SqlSelect;
import com.butent.bee.shared.Assert;
import com.butent.bee.shared.i18n.LocalizableConstants;
import com.butent.bee.shared.i18n.LocalizableMessages;
import com.butent.bee.shared.utils.BeeUtils;
import com.butent.bee.shared.utils.LogUtils;

import java.security.Principal;
import java.util.Collection;
import java.util.Locale;
import java.util.Map;
import java.util.logging.Logger;

import javax.annotation.PreDestroy;
import javax.annotation.Resource;
import javax.ejb.EJB;
import javax.ejb.EJBContext;
import javax.ejb.Lock;
import javax.ejb.LockType;
import javax.ejb.Singleton;

@Singleton
@Lock(LockType.READ)
public class UserServiceBean {

  public class UserInfo {
    private final String login;
    private final long userId;
    private final String firstName;
    private final String lastName;
    private final String position;
    private Collection<Long> userRoles;
    private boolean online = false;
    private Locale locale = Localized.defaultLocale;

    public UserInfo(long userId, String login, String firstName, String lastName, String position) {
      this.userId = userId;
      this.login = login;
      this.firstName = firstName;
      this.lastName = lastName;
      this.position = position;
    }

    public String getFirstName() {
      return firstName;
    }

    public String getLastName() {
      return lastName;
    }

    public Locale getLocale() {
      return locale;
    }

    public String getLogin() {
      return login;
    }

    public String getPosition() {
      return position;
    }

    public Collection<Long> getRoles() {
      return userRoles;
    }

    public long getUserId() {
      return userId;
    }

    public String getUserSign() {
      return BeeUtils.concat(1, getPosition(),
          BeeUtils.concat(1, BeeUtils.ifString(getFirstName(), getLogin()), getLastName()));
    }

    public boolean isOnline() {
      return online;
    }

    private UserInfo setLocale(String locale) {
      Locale loc = I18nUtils.toLocale(locale);

      if (BeeUtils.isEmpty(loc)) {
        LogUtils.warning(logger, getUserSign(), "Unknown user locale:", locale);
      } else {
        this.locale = loc;
      }
      return this;
    }

    private UserInfo setOnline(boolean online) {
      this.online = online;
      return this;
    }

    private UserInfo setRoles(Collection<Long> userRoles) {
      this.userRoles = userRoles;
      return this;
    }
  }

  private static Logger logger = Logger.getLogger(UserServiceBean.class.getName());

  public static final String USER_TABLE = "Users";
  public static final String ROLE_TABLE = "Roles";
  public static final String USER_ROLES_TABLE = "UserRoles";

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
    return Longs.toArray(getUserInfo(userId).getRoles());
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

  public LocalizableConstants localConstants() {
    return Localized.getConstants(getCurrentUserInfo().getLocale());
  }

  public LocalizableMessages localMesssages() {
    return Localized.getMessages(getCurrentUserInfo().getLocale());
  }

  @Lock(LockType.WRITE)
  public String login(String locale) {
    String sign = null;
    String user = getCurrentUser();

    if (isUser(user)) {
      UserInfo info = getUserInfo(user);
      sign = user + " " + BeeUtils.parenthesize(info.getUserSign());
      info.setOnline(true);
      info.setLocale(locale);
      LogUtils.infoNow(logger, "User logged in:", sign);

    } else if (BeeUtils.isEmpty(getUsers())) {
      sign = user;
      LogUtils.warning(logger, "Anonymous user logged in:", sign);

    } else {
      LogUtils.severe(logger, "Login attempt by an unauthorized user:", user);
    }
    return sign;
  }

  @Lock(LockType.WRITE)
  public void logout(String user) {
    if (isUser(user)) {
      UserInfo info = getUserInfo(user);
      String sign = user + " " + BeeUtils.parenthesize(info.getUserSign());

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
        logout(info.getLogin());
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

    String userIdName = sys.getIdName(USER_TABLE);
    String roleIdName = sys.getIdName(ROLE_TABLE);

    SqlSelect ss = new SqlSelect()
        .addFields("r", roleIdName, "Name")
        .addFrom(ROLE_TABLE, "r");

    for (Map<String, String> row : qs.getData(ss)) {
      roleCache.put(BeeUtils.toLong(row.get(roleIdName)), row.get("Name"));
    }

    ss = new SqlSelect()
        .addFields("r", "User", "Role")
        .addFrom(USER_ROLES_TABLE, "r");

    Multimap<Long, Long> userRoles = HashMultimap.create();

    for (Map<String, String> row : qs.getData(ss)) {
      userRoles.put(BeeUtils.toLong(row.get("User")), BeeUtils.toLong(row.get("Role")));
    }

    ss = new SqlSelect()
        .addFields("u", userIdName, "Login", "Position", "FirstName", "LastName")
        .addFrom(USER_TABLE, "u");

    for (Map<String, String> row : qs.getData(ss)) {
      long userId = BeeUtils.toLong(row.get(userIdName));
      String login = row.get("Login").toLowerCase();

      userCache.put(userId, login);

      UserInfo user = new UserInfo(userId, login,
          row.get("FirstName"), row.get("LastName"), row.get("Position"))
          .setRoles(userRoles.get(userId));

      UserInfo oldInfo = expiredCache.get(login);

      if (!BeeUtils.isEmpty(oldInfo)) {
        user.setLocale(oldInfo.getLocale().toString())
            .setOnline(oldInfo.isOnline());
      }
      infoCache.put(login, user);
    }
    cacheUpToDate = true;
  }
}
