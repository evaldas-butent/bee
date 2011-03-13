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
import com.butent.bee.shared.sql.SqlUtils;
import com.butent.bee.shared.utils.BeeUtils;
import com.butent.bee.shared.utils.LogUtils;

import java.security.Principal;
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

  private BiMap<Integer, String> userCache = HashBiMap.create();
  private BiMap<Integer, String> roleCache = HashBiMap.create();
  private Multimap<Integer, Integer> userRolesCache = HashMultimap.create();
  private Map<Integer, String> userInfoCache = Maps.newHashMap();

  private boolean cacheUpToDate = false;

  public String getCurrentUser() {
    Principal p = ctx.getCallerPrincipal();
    Assert.notEmpty(p);

    return p.getName().toLowerCase();
  }

  public int getCurrentUserId() {
    initUsers();
    String user = getCurrentUser();
    Assert.contains(userCache.inverse(), user);

    return userCache.inverse().get(user);
  }

  public Map<Integer, String> getRoles() {
    initUsers();
    return ImmutableMap.copyOf(roleCache);
  }

  public int[] getUserRoles(int userId) {
    initUsers();
    Assert.notEmpty(userId);
    Assert.contains(userCache, userId);
    return Ints.toArray(userRolesCache.get(userId));
  }

  public Map<Integer, String> getUsers() {
    initUsers();
    return ImmutableMap.copyOf(userCache);
  }

  public String getUserSign(String user) {
    initUsers();
    Assert.notEmpty(user);
    String sign = null;

    if (userCache.containsValue(user)) {
      sign = userInfoCache.get(userCache.inverse().get(user));
    } else if (BeeUtils.isEmpty(userCache)) {
      sign = user;
    }
    return sign;
  }

  @Lock(LockType.WRITE)
  public void invalidateCache() {
    cacheUpToDate = false;
  }

  public String login() {
    String usr = getUserSign(getCurrentUser());
    LogUtils.infoNow(logger, "User logged in:", usr);
    return usr;
  }

  public void logout(String user) {
    LogUtils.infoNow(logger, "User logged out:", getUserSign(user));
  }

  @Lock(LockType.WRITE)
  private void initUsers() {
    if (cacheUpToDate) {
      return;
    }
    userCache.clear();
    roleCache.clear();
    userRolesCache.clear();
    userInfoCache.clear();

    String userIdName = sys.getIdName(USER_TABLE);
    String roleIdName = sys.getIdName(ROLE_TABLE);

    SqlSelect ss = new SqlSelect()
        .addFields("r", roleIdName, "Name")
        .addFrom(ROLE_TABLE, "r");

    for (String[] row : qs.getData(ss)) {
      roleCache.put(BeeUtils.toInt(row[0]), row[1]);
    }

    ss = new SqlSelect()
        .addFields("u", userIdName, "Login", "Position", "FirstName", "LastName")
        .addFields("r", "Role")
        .addFrom(USER_TABLE, "u")
        .addFromLeft(USER_ROLES_TABLE, "r", SqlUtils.join("u", userIdName, "r", "User"));

    for (String[] row : qs.getData(ss)) {
      int userId = BeeUtils.toInt(row[0]);
      String login = row[1];

      userCache.put(userId, login.toLowerCase());
      userRolesCache.put(userId, BeeUtils.toInt(row[5]));
      userInfoCache.put(userId,
          BeeUtils.concat(1, row[2], BeeUtils.concat(1, BeeUtils.ifString(row[3], login), row[4])));
    }
    cacheUpToDate = true;
  }
}
