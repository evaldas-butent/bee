package com.butent.bee.server.rest;

import static com.butent.bee.server.rest.CrudWorker.*;
import static com.butent.bee.shared.modules.administration.AdministrationConstants.*;
import static com.butent.bee.shared.modules.classifiers.ClassifierConstants.*;
import static com.butent.bee.shared.modules.tasks.TaskConstants.*;

import com.butent.bee.server.data.QueryServiceBean;
import com.butent.bee.server.data.SystemBean;
import com.butent.bee.server.data.UserServiceBean;
import com.butent.bee.server.modules.administration.FileStorageBean;
import com.butent.bee.server.sql.SqlSelect;
import com.butent.bee.server.sql.SqlUtils;
import com.butent.bee.shared.data.SimpleRowSet;
import com.butent.bee.shared.ui.HasCaption;
import com.butent.bee.shared.utils.BeeUtils;

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

    SimpleRowSet rowSet = qs.getData(new SqlSelect()
        .addField(TBL_USERS, sys.getIdName(TBL_USERS), ID)
        .addField(TBL_USERS, sys.getVersionName(TBL_USERS), VERSION)
        .addField(TBL_USERS, COL_COMPANY_PERSON, COL_COMPANY_PERSON + ID)
        .addFields(TBL_USERS, COL_USER_BLOCK_FROM, COL_USER_BLOCK_UNTIL)
        .addFrom(TBL_USERS)
        .setWhere(Objects.nonNull(lastSynced)
            ? SqlUtils.more(TBL_USERS, sys.getVersionName(TBL_USERS), lastSynced) : null));

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

    SimpleRowSet.SimpleRow info = qs.getRow(new SqlSelect()
        .addConstant(userId, COL_USER + ID)
        .addField(TBL_COMPANY_PERSONS, COL_COMPANY, COL_COMPANY + ID)
        .addField(TBL_COMPANIES, COL_COMPANY_NAME, ALS_COMPANY_NAME)
        .addField(TBL_USERS, COL_COMPANY_PERSON, COL_COMPANY_PERSON + ID)
        .addFields(TBL_PERSONS, COL_FIRST_NAME, COL_LAST_NAME)
        .addFrom(TBL_USERS)
        .addFromInner(TBL_COMPANY_PERSONS,
            sys.joinTables(TBL_COMPANY_PERSONS, TBL_USERS, COL_COMPANY_PERSON))
        .addFromInner(TBL_PERSONS,
            sys.joinTables(TBL_PERSONS, TBL_COMPANY_PERSONS, COL_PERSON))
        .addFromInner(TBL_COMPANIES,
            sys.joinTables(TBL_COMPANIES, TBL_COMPANY_PERSONS, COL_COMPANY))
        .setWhere(sys.idEquals(TBL_USERS, userId)));

    Map<String, Object> map = new HashMap<>();

    for (String col : info.getColumnNames()) {
      map.put(col, BeeUtils.isSuffix(col, ID) ? info.getLong(col) : info.getValue(col));
    }
    map.put(COL_EMAIL, usr.getUserEmail(userId, true));
    resp.put(COL_USER, map);

    return RestResponse.ok(resp);
  }
}
