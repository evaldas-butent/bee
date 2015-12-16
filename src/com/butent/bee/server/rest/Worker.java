package com.butent.bee.server.rest;

import static com.butent.bee.server.rest.CrudWorker.*;
import static com.butent.bee.shared.modules.administration.AdministrationConstants.*;
import static com.butent.bee.shared.modules.classifiers.ClassifierConstants.*;
import static com.butent.bee.shared.modules.tasks.TaskConstants.*;

import com.butent.bee.server.data.QueryServiceBean;
import com.butent.bee.server.data.SystemBean;
import com.butent.bee.server.data.UserServiceBean;
import com.butent.bee.server.modules.administration.FileStorageBean;
import com.butent.bee.server.sql.IsCondition;
import com.butent.bee.server.sql.SqlSelect;
import com.butent.bee.server.sql.SqlUtils;
import com.butent.bee.shared.data.SimpleRowSet;
import com.butent.bee.shared.ui.HasCaption;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

import javax.ejb.EJB;
import javax.ws.rs.GET;
import javax.ws.rs.HeaderParam;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;

@Path("/")
public class Worker {

  @EJB
  QueryServiceBean qs;
  @EJB
  SystemBean sys;
  @EJB
  UserServiceBean usr;
  @EJB
  FileStorageBean fs;

  @GET
  @Path("users")
  @Produces(RestResponse.JSON_TYPE)
  public RestResponse getUsers(@HeaderParam(RestResponse.LAST_SYNC_TIME) Long lastSynced) {
    long time = System.currentTimeMillis();

    IsCondition clause = SqlUtils.or(
        SqlUtils.and(SqlUtils.isNull(TBL_USERS, COL_USER_BLOCK_FROM),
            SqlUtils.isNull(TBL_USERS, COL_USER_BLOCK_UNTIL)),
        SqlUtils.and(SqlUtils.notNull(TBL_USERS, COL_USER_BLOCK_FROM),
            SqlUtils.more(TBL_USERS, COL_USER_BLOCK_FROM, time)),
        SqlUtils.and(SqlUtils.notNull(TBL_USERS, COL_USER_BLOCK_UNTIL),
            SqlUtils.less(TBL_USERS, COL_USER_BLOCK_UNTIL, time)));

    if (Objects.nonNull(lastSynced)) {
      clause = SqlUtils.and(clause,
          SqlUtils.more(TBL_USERS, sys.getVersionName(TBL_USERS), lastSynced));
    }
    SimpleRowSet rowSet = qs.getData(new SqlSelect()
        .addField(TBL_USERS, sys.getIdName(TBL_USERS), ID)
        .addField(TBL_USERS, sys.getVersionName(TBL_USERS), VERSION)
        .addField(TBL_USERS, COL_COMPANY_PERSON, COL_COMPANY_PERSON + ID)
        .addFrom(TBL_USERS)
        .setWhere(clause));

    List<Map<String, Long>> data = new ArrayList<>();

    for (int i = 0; i < rowSet.getNumberOfRows(); i++) {
      Map<String, Long> row = new LinkedHashMap<>(rowSet.getNumberOfColumns());

      for (int j = 0; j < rowSet.getNumberOfColumns(); j++) {
        row.put(rowSet.getColumnName(j), rowSet.getLong(i, j));
      }
      data.add(row);
    }
    return RestResponse.ok(data).setLastSync(time);
  }

  @GET
  @Path("login")
  @Produces(RestResponse.JSON_TYPE)
  public RestResponse login() {
    Map<String, Object> resp = new HashMap<>();

    for (Class<? extends Enum<?>> aClass : Arrays.asList(TaskStatus.class,
        TaskPriority.class, TaskEvent.class)) {

      List<Object> list = new ArrayList<>();

      for (Enum<?> constant : aClass.getEnumConstants()) {
        Map<String, Object> map = new LinkedHashMap<>();
        map.put("Id", constant.ordinal());
        map.put("Caption", ((HasCaption) constant).getCaption());
        list.add(map);
      }
      resp.put(aClass.getSimpleName(), list);
    }
    Long userId = usr.getCurrentUserId();
    Map<String, Object> map = new LinkedHashMap<>();
    map.put(ID, userId);
    map.put(COL_USER, usr.getUserSign(userId));
    map.put(COL_EMAIL, usr.getUserEmail(userId, true));
    resp.put(COL_USER, map);

    return RestResponse.ok(resp);
  }
}
