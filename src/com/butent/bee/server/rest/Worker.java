package com.butent.bee.server.rest;

import static com.butent.bee.server.rest.CrudWorker.*;
import static com.butent.bee.shared.modules.administration.AdministrationConstants.*;
import static com.butent.bee.shared.modules.classifiers.ClassifierConstants.*;
import static com.butent.bee.shared.modules.documents.DocumentConstants.*;

import com.butent.bee.server.data.QueryServiceBean;
import com.butent.bee.server.data.SystemBean;
import com.butent.bee.server.data.UserServiceBean;
import com.butent.bee.server.modules.administration.FileStorageBean;
import com.butent.bee.server.rest.annotations.Trusted;
import com.butent.bee.server.sql.IsCondition;
import com.butent.bee.server.sql.SqlSelect;
import com.butent.bee.server.sql.SqlUtils;
import com.butent.bee.shared.data.SimpleRowSet;
import com.butent.bee.shared.utils.BeeUtils;
import com.butent.bee.shared.utils.Codec;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

import javax.ejb.EJB;
import javax.ws.rs.GET;
import javax.ws.rs.HeaderParam;
import javax.ws.rs.NotFoundException;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.UriBuilder;

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
  @Path("{api}.pdf")
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
    return new FileServiceApplication().getFile(name + ".pdf",
        Codec.encodeBase64(fs.createPdf(content)));
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
  @Path("users")
  @Produces(RestResponse.JSON_TYPE)
  public RestResponse getUsers(@HeaderParam(RestResponse.LAST_SYNC_TIME) Long lastSynced) {
    long time = System.currentTimeMillis();

    IsCondition clause = SqlUtils.or(
        SqlUtils.and(SqlUtils.isNull(TBL_USERS, COL_USER_BLOCK_AFTER),
            SqlUtils.isNull(TBL_USERS, COL_USER_BLOCK_BEFORE)),
        SqlUtils.and(SqlUtils.notNull(TBL_USERS, COL_USER_BLOCK_AFTER),
            SqlUtils.more(TBL_USERS, COL_USER_BLOCK_AFTER, time)),
        SqlUtils.and(SqlUtils.notNull(TBL_USERS, COL_USER_BLOCK_BEFORE),
            SqlUtils.less(TBL_USERS, COL_USER_BLOCK_BEFORE, time)));

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
  public Response login() {
    return Response.temporaryRedirect(UriBuilder.fromResource(CompanyPersonsWorker.class)
        .path("{id}").build(usr.getCompanyPerson(usr.getCurrentUserId()))).build();
  }
}
