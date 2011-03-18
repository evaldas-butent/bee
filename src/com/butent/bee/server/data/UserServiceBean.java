package com.butent.bee.server.data;

import com.google.common.collect.BiMap;
import com.google.common.collect.HashBiMap;
import com.google.common.collect.HashMultimap;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Maps;
import com.google.common.collect.Multimap;
import com.google.common.primitives.Longs;

import com.butent.bee.shared.Assert;
import com.butent.bee.shared.sql.SqlSelect;
import com.butent.bee.shared.utils.BeeUtils;
import com.butent.bee.shared.utils.LogUtils;

import java.security.Principal;
import java.util.Collection;
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
    private String login;
    private long userId;
    private String firstName;
    private String lastName;
    private String position;
    private Collection<Long> userRoles;
    private boolean online = false;

    public UserInfo(long userId, String login, String firstName, String lastName, String position) {
      this.userId = userId;
      this.login = login;
      this.firstName = firstName;
      this.lastName = lastName;
      this.position = position;
    }

    public String firstName() {
      return firstName;
    }

    public Collection<Long> getRoles() {
      return userRoles;
    }

    public String getUserSign() {
      return BeeUtils.concat(1, position(),
          BeeUtils.concat(1, BeeUtils.ifString(firstName(), login()), lastName()));
    }

    public boolean isOnline() {
      return online;
    }

    public String lastName() {
      return lastName;
    }

    public String login() {
      return login;
    }

    public String position() {
      return position;
    }

    public long userId() {
      return userId;
    }

    private void setOnline(boolean online) {
      this.online = online;
    }

    private void setRoles(Collection<Long> userRoles) {
      this.userRoles = userRoles;
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

  private BiMap<Long, String> roles = HashBiMap.create();
  private BiMap<String, Long> users = HashBiMap.create();
  private Map<Long, UserInfo> userCache = Maps.newHashMap();

  private boolean cacheUpToDate = false;

  public String getCurrentUser() {
    Principal p = ctx.getCallerPrincipal();
    Assert.notEmpty(p);

    return p.getName().toLowerCase();
  }

  public long getCurrentUserId() {
    initUsers();
    String user = getCurrentUser();
    Assert.contains(users, user);

    return users.get(user);
  }

  public Map<Long, String> getRoles() {
    initUsers();
    return ImmutableMap.copyOf(roles);
  }

  public long[] getUserRoles(long userId) {
    initUsers();
    Assert.notEmpty(userId);
    return Longs.toArray(getUser(userId).getRoles());
  }

  public Map<Long, String> getUsers() {
    initUsers();
    return ImmutableMap.copyOf(users.inverse());
  }

  @Lock(LockType.WRITE)
  public void invalidateCache() {
    cacheUpToDate = false; // TODO: sukontroliuoti user online būsenas
  }

  public String login() {
    String sign = null;
    String usr = getCurrentUser();
    UserInfo user = getUser(usr);

    if (!BeeUtils.isEmpty(user)) {
      sign = user.getUserSign();
      user.setOnline(true);
      LogUtils.infoNow(logger, "User logged in:", sign);

    } else if (BeeUtils.isEmpty(users)) {
      sign = usr;
      LogUtils.warning(logger, "Anonymous user logged in:", sign);

    } else {
      LogUtils.severe(logger, "Login attempt by an unauthorized user:", usr);
    }
    return sign;
  }

  public void logout(String usr) {
    UserInfo user = getUser(usr);

    if (!BeeUtils.isEmpty(user)) {
      user.setOnline(false);
      LogUtils.infoNow(logger, "User logged out:", user.getUserSign());

    } else if (BeeUtils.isEmpty(users)) {
      LogUtils.warning(logger, "Anonymous user logged out:", usr);

    } else {
      LogUtils.severe(logger, "Logout attempt by an unauthorized user:", usr);
    }
  }

  @SuppressWarnings("unused")
  @PreDestroy
  @Lock(LockType.WRITE)
  private void destroy() {
    for (UserInfo user : userCache.values()) {
      if (user.isOnline()) {
        logout(user.login());
      }
    }
  }

  private UserInfo getUser(long userId) {
    initUsers();
    Assert.contains(userCache, userId);
    return userCache.get(userId);
  }

  private UserInfo getUser(String usr) {
    initUsers();
    return userCache.get(users.get(usr));
  }

  @Lock(LockType.WRITE)
  private void initUsers() {
    if (cacheUpToDate) {
      return;
    }
    roles.clear();
    users.clear();
    userCache.clear();

    String userIdName = sys.getIdName(USER_TABLE);
    String roleIdName = sys.getIdName(ROLE_TABLE);

    SqlSelect ss = new SqlSelect()
        .addFields("r", roleIdName, "Name")
        .addFrom(ROLE_TABLE, "r");

    for (Map<String, String> row : qs.getData(ss)) {
      roles.put(BeeUtils.toLong(row.get(roleIdName)), row.get("Name"));
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
      String login = row.get("Login");

      users.put(login.toLowerCase(), userId);

      UserInfo user = new UserInfo(userId, login, row.get("FirstName"), row.get("LastName"),
          row.get("Position"));

      user.setRoles(userRoles.get(userId));
      userCache.put(userId, user);
    }
    cacheUpToDate = true;
  }
}
