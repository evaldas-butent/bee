package com.butent.bee.server.data;

import com.google.common.collect.BiMap;
import com.google.common.collect.HashBiMap;
import com.google.common.collect.HashMultimap;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Maps;
import com.google.common.collect.Multimap;
import com.google.common.primitives.Ints;

import com.butent.bee.shared.Assert;
import com.butent.bee.shared.sql.SqlSelect;
import com.butent.bee.shared.utils.BeeUtils;
import com.butent.bee.shared.utils.LogUtils;

import java.security.Principal;
import java.util.Collection;
import java.util.Map;
import java.util.logging.Logger;

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
    private int userId;
    private String firstName;
    private String lastName;
    private String position;
    private Collection<Integer> userRoles;
    private boolean online = false;

    public UserInfo(int userId, String login, String firstName, String lastName, String position) {
      this.userId = userId;
      this.login = login;
      this.firstName = firstName;
      this.lastName = lastName;
      this.position = position;
    }

    public String firstName() {
      return firstName;
    }

    public Collection<Integer> getRoles() {
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

    public int userId() {
      return userId;
    }

    private void setOnline(boolean online) {
      this.online = online;
    }

    private void setRoles(Collection<Integer> userRoles) {
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

  private BiMap<Integer, String> roles = HashBiMap.create();
  private BiMap<String, Integer> users = HashBiMap.create();
  private Map<Integer, UserInfo> userInfoCache = Maps.newHashMap();

  private boolean cacheUpToDate = false;

  public String getCurrentUser() {
    Principal p = ctx.getCallerPrincipal();
    Assert.notEmpty(p);

    return p.getName().toLowerCase();
  }

  public int getCurrentUserId() {
    initUsers();
    String user = getCurrentUser();
    Assert.contains(users, user);

    return users.get(user);
  }

  public Map<Integer, String> getRoles() {
    initUsers();
    return ImmutableMap.copyOf(roles);
  }

  public int[] getUserRoles(int userId) {
    initUsers();
    Assert.notEmpty(userId);
    return Ints.toArray(getUser(userId).getRoles());
  }

  public Map<Integer, String> getUsers() {
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
      LogUtils.severe(logger, "Unauthorized login:", usr);
    }
    return sign;
  }

  public void logout(String usr) {
    UserInfo user = getUser(usr);

    if (BeeUtils.isEmpty(user)) {
      LogUtils.warning(logger, "Unknown user:", usr);
    } else {
      user.setOnline(false);
      LogUtils.infoNow(logger, "User logged out:", user.getUserSign());
    }
  }

  private UserInfo getUser(int userId) {
    initUsers();
    Assert.contains(userInfoCache, userId);
    return userInfoCache.get(userId);
  }

  private UserInfo getUser(String usr) {
    initUsers();
    return userInfoCache.get(users.get(usr));
  }

  @Lock(LockType.WRITE)
  private void initUsers() {
    if (cacheUpToDate) {
      return;
    }
    roles.clear();
    users.clear();
    userInfoCache.clear();

    String userIdName = sys.getIdName(USER_TABLE);
    String roleIdName = sys.getIdName(ROLE_TABLE);

    SqlSelect ss = new SqlSelect()
        .addFields("r", roleIdName, "Name")
        .addFrom(ROLE_TABLE, "r");

    for (Map<String, String> row : qs.getData(ss)) {
      roles.put(BeeUtils.toInt(row.get(roleIdName)), row.get("Name"));
    }

    ss = new SqlSelect()
        .addFields("r", "User", "Role")
        .addFrom(USER_ROLES_TABLE, "r");

    Multimap<Integer, Integer> userRoles = HashMultimap.create();

    for (Map<String, String> row : qs.getData(ss)) {
      userRoles.put(BeeUtils.toInt(row.get("User")), BeeUtils.toInt(row.get("Role")));
    }

    ss = new SqlSelect()
        .addFields("u", userIdName, "Login", "Position", "FirstName", "LastName")
        .addFrom(USER_TABLE, "u");

    for (Map<String, String> row : qs.getData(ss)) {
      int userId = BeeUtils.toInt(row.get(userIdName));
      String login = row.get("Login");

      users.put(login.toLowerCase(), userId);

      UserInfo user = new UserInfo(userId, login, row.get("FirstName"), row.get("LastName"),
          row.get("Position"));

      user.setRoles(userRoles.get(userId));
      userInfoCache.put(userId, user);
    }
    cacheUpToDate = true;
  }
}
