package com.butent.bee.server.rest;

import static com.butent.bee.shared.modules.classifiers.ClassifierConstants.*;

import com.butent.bee.server.data.QueryServiceBean;
import com.butent.bee.server.data.SystemBean;
import com.butent.bee.server.rest.annotations.Trusted;
import com.butent.bee.server.sql.SqlSelect;
import com.butent.bee.server.sql.SqlUtils;
import com.butent.bee.shared.BeeConst;
import com.butent.bee.shared.data.SimpleRowSet;
import com.butent.bee.shared.utils.BeeUtils;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

import javax.ejb.EJB;
import javax.ws.rs.GET;
import javax.ws.rs.HeaderParam;
import javax.ws.rs.NotFoundException;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

@Path("/")
public class Worker {

  private static final String LAST_SYNC_TIME = "LastSyncTime";
  private static final String ID = "ID";

  @EJB
  QueryServiceBean qs;
  @EJB
  SystemBean sys;

  @GET
  @Path("companies")
  @Produces(MediaType.APPLICATION_JSON)
  public Response companies(@HeaderParam(LAST_SYNC_TIME) Long lastSynced) {
    long time = System.currentTimeMillis();

    SqlSelect query = new SqlSelect()
        .addField(TBL_COMPANIES, sys.getIdName(TBL_COMPANIES), ID)
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
      query.setWhere(SqlUtils.or(
          SqlUtils.more(TBL_COMPANIES, sys.getVersionName(TBL_COMPANIES), lastSynced),
          SqlUtils.more(TBL_CONTACTS, sys.getVersionName(TBL_CONTACTS), lastSynced)));
    }
    SimpleRowSet companies = qs.getData(query);

    Response response = rowSetResponse(companies);
    response.getHeaders().add(LAST_SYNC_TIME, time);

    return response;
  }

  @GET
  @Path("endpoint")
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
  @Path("login")
  public Response login() {
    return Response.ok().build();
  }

  private static Response rowSetResponse(SimpleRowSet rowSet) {
    List<Map<String, String>> data = new ArrayList<>();

    for (int i = 0; i < rowSet.getNumberOfRows(); i++) {
      Map<String, String> row = new HashMap<>(rowSet.getNumberOfColumns());

      for (int j = 0; j < rowSet.getNumberOfColumns(); j++) {
        row.put(rowSet.getColumnName(j), rowSet.getValue(i, j));
      }
      data.add(row);
    }
    return Response.ok(data, MediaType.APPLICATION_JSON_TYPE.withCharset(BeeConst.CHARSET_UTF8))
        .build();
  }
}
