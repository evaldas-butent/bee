package com.butent.bee.server.data;

import com.butent.bee.shared.Assert;
import com.butent.bee.shared.data.BeeRowSet.BeeRow;
import com.butent.bee.shared.sql.SqlSelect;
import com.butent.bee.shared.sql.SqlUtils;

import java.security.Principal;
import java.util.LinkedHashMap;
import java.util.List;
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

  private static final String USER_TABLE = "Users";
  private static final String ROLE_TABLE = "Roles";
  private static final String USER_ROLES_TABLE = "UserRoles";

  @Resource
  EJBContext ctx;
  @EJB
  SystemBean sys;
  @EJB
  QueryServiceBean qs;

  public String getCurrentUser() {
    Principal p = ctx.getCallerPrincipal();
    Assert.notEmpty(p);

    return p.getName();
  }

  public int getCurrentUserId() {
    String idName = sys.getIdName(USER_TABLE);
    String user = getCurrentUser();

    return qs.getSingleRow(new SqlSelect()
      .addFields("u", idName)
      .addFrom(USER_TABLE, "u")
      .setWhere(SqlUtils.equal("u", "Login", user))).getInt(0);
  }

  public Map<Integer, String> getRoles() {
    String idName = sys.getIdName(ROLE_TABLE);

    SqlSelect ss = new SqlSelect()
      .addFields("r", idName, "Name")
      .addFrom(ROLE_TABLE, "r")
      .addOrder("r", "Name");

    Map<Integer, String> roles = new LinkedHashMap<Integer, String>();

    for (BeeRow role : qs.getData(ss).getRows()) {
      roles.put(role.getInt(idName), role.getString("Name"));
    }
    return roles;
  }

  public int[] getUserRoles(int userId) {
    Assert.notEmpty(userId);

    SqlSelect ss = new SqlSelect()
      .addFields("r", "Role")
      .addFrom(USER_ROLES_TABLE, "r")
      .setWhere(SqlUtils.equal("r", "User", userId));

    List<BeeRow> rows = qs.getData(ss).getRows();
    int[] roles = new int[rows.size()];

    for (int i = 0; i < rows.size(); i++) {
      roles[i] = rows.get(i).getInt(0);
    }
    return roles;
  }

  public Map<Integer, String> getUsers() {
    String idName = sys.getIdName(USER_TABLE);

    SqlSelect ss = new SqlSelect()
      .addFields("u", idName, "Login")
      .addFrom(USER_TABLE, "u")
      .addOrder("u", "Login");

    Map<Integer, String> users = new LinkedHashMap<Integer, String>();

    for (BeeRow user : qs.getData(ss).getRows()) {
      users.put(user.getInt(idName), user.getString("Login"));
    }
    return users;
  }
}
