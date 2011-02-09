package com.butent.bee.server.data;

import com.google.common.collect.BiMap;
import com.google.common.collect.HashBiMap;
import com.google.common.collect.HashMultimap;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Maps;
import com.google.common.collect.Multimap;
import com.google.common.primitives.Ints;

import com.butent.bee.shared.Assert;
import com.butent.bee.shared.data.BeeRow;
import com.butent.bee.shared.data.BeeRowSet;
import com.butent.bee.shared.sql.SqlSelect;
import com.butent.bee.shared.sql.SqlUtils;
import com.butent.bee.shared.utils.BeeUtils;

import java.security.Principal;
import java.util.Map;

import javax.annotation.Resource;
import javax.ejb.EJB;
import javax.ejb.EJBContext;
import javax.ejb.Lock;
import javax.ejb.LockType;
import javax.ejb.Singleton;

@Singleton
@Lock(LockType.READ)
public class UserServiceBean {

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

  public String getUserSign() {
    initUsers();
    String user = getCurrentUser();
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
      .addFields("r", "Name", roleIdName)
      .addFrom(ROLE_TABLE, "r");
    
    BeeRowSet brs = qs.getData(ss);
    for (BeeRow role : brs.getRows()) {
      roleCache.put(brs.getInt(role, roleIdName), brs.getString(role, "Name"));
    }

    ss = new SqlSelect()
      .addFields("u", "Login", userIdName, "FirstName", "LastName", "Position")
      .addFields("r", "Role")
      .addFrom(USER_TABLE, "u")
      .addFromLeft(USER_ROLES_TABLE, "r", SqlUtils.join("u", userIdName, "r", "User"));
    
    brs = qs.getData(ss);
    for (BeeRow user : brs.getRows()) {
      int userId = brs.getInt(user, userIdName);

      userCache.put(userId, brs.getString(user, "Login").toLowerCase());
      userRolesCache.put(userId, brs.getInt(user, "Role"));
      userInfoCache.put(userId,
          BeeUtils.concat(1, brs.getString(user, "Position"),
              BeeUtils.concat(1,
                  BeeUtils.ifString(brs.getString(user, "FirstName"), brs.getString(user, "Login")),
                  brs.getString(user, "LastName"))));
    }
    cacheUpToDate = true;
  }
}
