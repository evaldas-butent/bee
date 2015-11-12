package com.butent.bee.server.rest;

import static com.butent.bee.shared.modules.administration.AdministrationConstants.*;
import static com.butent.bee.shared.modules.classifiers.ClassifierConstants.*;

import com.butent.bee.server.data.QueryServiceBean;
import com.butent.bee.server.data.SystemBean;
import com.butent.bee.server.sql.IsExpression;
import com.butent.bee.server.sql.SqlSelect;
import com.butent.bee.server.sql.SqlUtils;
import com.butent.bee.shared.BeeConst;
import com.butent.bee.shared.data.SimpleRowSet;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

import javax.ejb.EJB;
import javax.ws.rs.GET;
import javax.ws.rs.HeaderParam;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

@Path("/")
public class Worker {

  private static final String LAST_SYNC_TIME = "LastSyncTime";
  private static final String ID = "ID";
  private static final String VERSION = "VERSION";

  @EJB
  QueryServiceBean qs;
  @EJB
  SystemBean sys;

  @GET
  @Path("companies")
  @Produces(MediaType.APPLICATION_JSON)
  public Response companies(@HeaderParam(LAST_SYNC_TIME) Long lastSynced) {
    long time = System.currentTimeMillis();
    IsExpression version1 = SqlUtils.field(TBL_COMPANIES, sys.getVersionName(TBL_COMPANIES));
    IsExpression version2 = SqlUtils.field(TBL_CONTACTS, sys.getVersionName(TBL_CONTACTS));

    SqlSelect query = new SqlSelect()
        .addField(TBL_COMPANIES, sys.getIdName(TBL_COMPANIES), ID)
        .addExpr(SqlUtils.sqlIf(SqlUtils.more(version1, version2), version1, version2), VERSION)
        .addFields(TBL_COMPANIES, COL_COMPANY_NAME, COL_COMPANY_CODE)
        .addFields(TBL_CONTACTS, COL_PHONE, COL_MOBILE, COL_ADDRESS)
        .addFields(TBL_EMAILS, COL_EMAIL_ADDRESS)
        .addField(TBL_CITIES, COL_CITY_NAME, COL_CITY)
        .addField(TBL_COUNTRIES, COL_COUNTRY_NAME, COL_COUNTRY)
        .addFrom(TBL_COMPANIES)
        .addFromLeft(TBL_CONTACTS, sys.joinTables(TBL_CONTACTS, TBL_COMPANIES, COL_CONTACT))
        .addFromLeft(TBL_EMAILS, sys.joinTables(TBL_EMAILS, TBL_CONTACTS, COL_EMAIL))
        .addFromLeft(TBL_CITIES, sys.joinTables(TBL_CITIES, TBL_CONTACTS, COL_CITY))
        .addFromLeft(TBL_COUNTRIES, sys.joinTables(TBL_COUNTRIES, TBL_CONTACTS, COL_COUNTRY));

    if (Objects.nonNull(lastSynced)) {
      query.setWhere(SqlUtils.or(SqlUtils.more(version1, lastSynced),
          SqlUtils.more(version2, lastSynced)));
    }
    SimpleRowSet companies = qs.getData(query);

    Response response = rowSetResponse(companies);
    response.getHeaders().add(LAST_SYNC_TIME, time);

    return response;
  }

  @GET
  @Path("companypersons")
  @Produces(MediaType.APPLICATION_JSON)
  public Response companyPersons(@HeaderParam(LAST_SYNC_TIME) Long lastSynced) {
    long time = System.currentTimeMillis();
    IsExpression version1 = SqlUtils.field(TBL_COMPANY_PERSONS,
        sys.getVersionName(TBL_COMPANY_PERSONS));
    IsExpression version2 = SqlUtils.field(TBL_CONTACTS, sys.getVersionName(TBL_CONTACTS));

    SqlSelect query = new SqlSelect()
        .addField(TBL_COMPANY_PERSONS, sys.getIdName(TBL_COMPANY_PERSONS), ID)
        .addExpr(SqlUtils.sqlIf(SqlUtils.more(version1, version2), version1, version2), VERSION)
        .addFields(TBL_COMPANY_PERSONS, COL_COMPANY, COL_PERSON)
        .addFields(TBL_PERSONS, COL_FIRST_NAME, COL_LAST_NAME)
        .addField(TBL_POSITIONS, COL_POSITION_NAME, COL_POSITION)
        .addFields(TBL_CONTACTS, COL_PHONE, COL_MOBILE, COL_ADDRESS)
        .addFields(TBL_EMAILS, COL_EMAIL_ADDRESS)
        .addField(TBL_CITIES, COL_CITY_NAME, COL_CITY)
        .addField(TBL_COUNTRIES, COL_COUNTRY_NAME, COL_COUNTRY)
        .addFrom(TBL_COMPANY_PERSONS)
        .addFromInner(TBL_PERSONS, sys.joinTables(TBL_PERSONS, TBL_COMPANY_PERSONS, COL_PERSON))
        .addFromLeft(TBL_POSITIONS,
            sys.joinTables(TBL_POSITIONS, TBL_COMPANY_PERSONS, COL_POSITION))
        .addFromLeft(TBL_CONTACTS, sys.joinTables(TBL_CONTACTS, TBL_COMPANY_PERSONS, COL_CONTACT))
        .addFromLeft(TBL_EMAILS, sys.joinTables(TBL_EMAILS, TBL_CONTACTS, COL_EMAIL))
        .addFromLeft(TBL_CITIES, sys.joinTables(TBL_CITIES, TBL_CONTACTS, COL_CITY))
        .addFromLeft(TBL_COUNTRIES, sys.joinTables(TBL_COUNTRIES, TBL_CONTACTS, COL_COUNTRY));

    if (Objects.nonNull(lastSynced)) {
      query.setWhere(SqlUtils.or(SqlUtils.more(version1, lastSynced),
          SqlUtils.more(version2, lastSynced)));
    }
    SimpleRowSet companyPersons = qs.getData(query);

    Response response = rowSetResponse(companyPersons);
    response.getHeaders().add(LAST_SYNC_TIME, time);

    return response;
  }

  @GET
  @Path("login")
  public Response login() {
    return Response.ok().build();
  }

  @GET
  @Path("users")
  @Produces(MediaType.APPLICATION_JSON)
  public Response users(@HeaderParam(LAST_SYNC_TIME) Long lastSynced) {
    long time = System.currentTimeMillis();

    SqlSelect query = new SqlSelect()
        .addField(TBL_USERS, sys.getIdName(TBL_USERS), ID)
        .addField(TBL_USERS, sys.getVersionName(TBL_USERS), VERSION)
        .addFields(TBL_USERS, COL_COMPANY_PERSON, COL_USER_BLOCK_BEFORE, COL_USER_BLOCK_AFTER)
        .addFrom(TBL_USERS);

    if (Objects.nonNull(lastSynced)) {
      query.setWhere(SqlUtils.more(TBL_USERS, sys.getVersionName(TBL_USERS), lastSynced));
    }
    SimpleRowSet users = qs.getData(query);

    Response response = rowSetResponse(users);
    response.getHeaders().add(LAST_SYNC_TIME, time);

    return response;
  }

  private static Response rowSetResponse(SimpleRowSet rowSet) {
    List<Map<String, String>> data = new ArrayList<>();

    for (int i = 0; i < rowSet.getNumberOfRows(); i++) {
      Map<String, String> row = new LinkedHashMap<>(rowSet.getNumberOfColumns());

      for (int j = 0; j < rowSet.getNumberOfColumns(); j++) {
        row.put(rowSet.getColumnName(j), rowSet.getValue(i, j));
      }
      data.add(row);
    }
    return Response.ok(data, MediaType.APPLICATION_JSON_TYPE.withCharset(BeeConst.CHARSET_UTF8))
        .build();
  }
}
