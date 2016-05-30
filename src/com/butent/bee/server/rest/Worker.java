package com.butent.bee.server.rest;

import static com.butent.bee.server.rest.CrudWorker.*;
import static com.butent.bee.shared.modules.administration.AdministrationConstants.*;
import static com.butent.bee.shared.modules.classifiers.ClassifierConstants.*;
import static com.butent.bee.shared.modules.documents.DocumentConstants.*;
import static com.butent.bee.shared.modules.tasks.TaskConstants.*;

import com.butent.bee.server.Invocation;
import com.butent.bee.server.data.QueryServiceBean;
import com.butent.bee.server.data.SystemBean;
import com.butent.bee.server.data.UserServiceBean;
import com.butent.bee.server.modules.administration.FileStorageBean;
import com.butent.bee.server.rest.annotations.Trusted;
import com.butent.bee.server.sql.SqlSelect;
import com.butent.bee.server.sql.SqlUtils;
import com.butent.bee.shared.data.BeeRow;
import com.butent.bee.shared.data.BeeRowSet;
import com.butent.bee.shared.data.SimpleRowSet;
import com.butent.bee.shared.data.filter.Filter;
import com.butent.bee.shared.modules.tasks.TaskConstants.*;
import com.butent.bee.shared.ui.HasCaption;
import com.butent.bee.shared.utils.ArrayUtils;
import com.butent.bee.shared.utils.BeeUtils;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

import javax.ejb.EJB;
import javax.ejb.Stateless;
import javax.ws.rs.GET;
import javax.ws.rs.HeaderParam;
import javax.ws.rs.NotFoundException;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

@Path("/")
@Produces(RestResponse.JSON_TYPE)
@Stateless
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
  @Path("companytypes")
  public RestResponse getCompanyTypes(@HeaderParam(RestResponse.LAST_SYNC_TIME) Long lastSynced) {
    long time = System.currentTimeMillis();

    return RestResponse
        .ok(rsToMap(getRowSet(TBL_COMPANY_TYPES, COL_COMPANY_TYPE_NAME, lastSynced)))
        .setLastSync(time);
  }

  @GET
  @Path("cities")
  public RestResponse getCities(@HeaderParam(RestResponse.LAST_SYNC_TIME) Long lastSynced) {
    long time = System.currentTimeMillis();

    return RestResponse.ok(rsToMap(getRowSet(TBL_CITIES, COL_CITY_NAME, lastSynced, COL_COUNTRY)))
        .setLastSync(time);
  }

  @GET
  @Path("countries")
  public RestResponse getCountries(@HeaderParam(RestResponse.LAST_SYNC_TIME) Long lastSynced) {
    long time = System.currentTimeMillis();

    return RestResponse.ok(rsToMap(getRowSet(TBL_COUNTRIES, COL_COUNTRY_NAME, lastSynced)))
        .setLastSync(time);
  }

  @GET
  @Path("{api}.pdf")
  @Produces(MediaType.APPLICATION_OCTET_STREAM)
  @Trusted
  public Response getApi(@PathParam("api") String name) {
    String content = qs.getValue(new SqlSelect()
        .addFields(TBL_DOCUMENT_DATA, COL_DOCUMENT_CONTENT)
        .addFrom(TBL_DOCUMENTS)
        .addFromInner(TBL_DOCUMENT_DATA,
            sys.joinTables(TBL_DOCUMENT_DATA, TBL_DOCUMENTS, COL_DOCUMENT_DATA))
        .setWhere(SqlUtils.equals(TBL_DOCUMENTS, COL_DOCUMENT_NAME, name)));

    if (BeeUtils.isEmpty(content)) {
      throw new NotFoundException();
    }
    return Invocation.locateRemoteBean(FileServiceApplication.class)
        .getFile(fs.createPdf(content), name + ".pdf");
  }

  @GET
  @Path("durationtypes")
  public RestResponse getDurationTypes(@HeaderParam(RestResponse.LAST_SYNC_TIME) Long lastSynced) {
    long time = System.currentTimeMillis();

    return RestResponse.ok(rsToMap(getRowSet(TBL_DURATION_TYPES, COL_DURATION_TYPE_NAME,
        lastSynced))).setLastSync(time);
  }

  @GET
  @Path("tasktypes")
  public RestResponse getTaskTypes(@HeaderParam(RestResponse.LAST_SYNC_TIME) Long lastSynced) {
    long time = System.currentTimeMillis();

    return RestResponse.ok(rsToMap(getRowSet(TBL_TASK_TYPES, COL_TASK_TYPE_NAME, lastSynced)))
        .setLastSync(time);
  }

  @GET
  @Path("endpoint")
  @Produces(MediaType.TEXT_PLAIN)
  @Trusted(secret = "B-NOVO")
  public String getPath(@HeaderParam("licence") String licence) {
    String endpoint = null;

    if (!BeeUtils.isEmpty(licence)) {
      endpoint = qs.getValue(new SqlSelect()
          .addFields(TBL_COMPANY_LICENCES, COL_LICENCE_ENDPOINT)
          .addFrom(TBL_COMPANY_LICENCES)
          .setWhere(SqlUtils.equals(TBL_COMPANY_LICENCES, COL_LICENCE, licence)));
    }
    if (BeeUtils.isEmpty(endpoint)) {
      throw new NotFoundException(licence);
    }
    return endpoint;
  }

  @GET
  @Path("users")
  public RestResponse getUsers(@HeaderParam(RestResponse.LAST_SYNC_TIME) Long lastSynced) {
    long time = System.currentTimeMillis();

    BeeRowSet users = (BeeRowSet) qs.doSql(new SqlSelect()
        .addField(TBL_USERS, sys.getIdName(TBL_USERS), ID)
        .addField(TBL_USERS, sys.getVersionName(TBL_USERS), VERSION)
        .addField(TBL_USERS, COL_COMPANY_PERSON, COL_COMPANY_PERSON + ID)
        .addFields(TBL_USERS, COL_USER_BLOCK_FROM, COL_USER_BLOCK_UNTIL)
        .addFrom(TBL_USERS)
        .setWhere(Objects.nonNull(lastSynced)
            ? SqlUtils.more(TBL_USERS, sys.getVersionName(TBL_USERS), lastSynced) : null)
        .getQuery());

    BeeRowSet persons = qs.getViewData(new CompanyPersonsWorker().getViewName(),
        Filter.idIn(users.getDistinctLongs(users.getColumnIndex(COL_COMPANY_PERSON + ID))));

    Collection<Map<String, Object>> data = rsToMap(users);

    for (Map<String, Object> user : data) {
      int r = persons.getRowIndex((Long) user.get(COL_COMPANY_PERSON + ID));
      BeeRow row = persons.getRow(r);
      Map<String, Object> person = new LinkedHashMap<>();
      person.put(ID, row.getId());
      person.put(VERSION, row.getVersion());

      for (int c = 0; c < persons.getNumberOfColumns(); c++) {
        person.put(persons.getColumnId(c), CrudWorker.getValue(persons, r, c));
      }
      user.put(COL_COMPANY_PERSON, person);
    }
    return RestResponse.ok(data).setLastSync(time);
  }

  @GET
  @Path("login")
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

  private BeeRowSet getRowSet(String table, String field, Long lastSynced, String... fields) {
    SqlSelect select = new SqlSelect()
        .addField(table, sys.getIdName(table), ID)
        .addField(table, sys.getVersionName(table), VERSION)
        .addField(table, field, "Name")
        .addFrom(table)
        .setWhere(Objects.nonNull(lastSynced)
            ? SqlUtils.more(table, sys.getVersionName(table), lastSynced)
            : null);

    if (!ArrayUtils.isEmpty(fields)) {
      select.addFields(table, fields);
    }
    return (BeeRowSet) qs.doSql(select.getQuery());
  }

  private static Collection<Map<String, Object>> rsToMap(BeeRowSet rowSet) {
    List<Map<String, Object>> data = new ArrayList<>();

    for (int i = 0; i < rowSet.getNumberOfRows(); i++) {
      Map<String, Object> row = new LinkedHashMap<>(rowSet.getNumberOfColumns());

      for (int j = 0; j < rowSet.getNumberOfColumns(); j++) {
        row.put(rowSet.getColumnId(j), CrudWorker.getValue(rowSet, i, j));
      }
      data.add(row);
    }
    return data;
  }
}
