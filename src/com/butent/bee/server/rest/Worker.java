package com.butent.bee.server.rest;

import static com.butent.bee.server.rest.CrudWorker.*;
import static com.butent.bee.shared.modules.administration.AdministrationConstants.*;
import static com.butent.bee.shared.modules.classifiers.ClassifierConstants.COL_COMPANY_PERSON;
import static com.butent.bee.shared.modules.documents.DocumentConstants.*;

import com.butent.bee.server.data.QueryServiceBean;
import com.butent.bee.server.data.SystemBean;
import com.butent.bee.server.modules.administration.FileStorageBean;
import com.butent.bee.server.rest.annotations.Trusted;
import com.butent.bee.server.sql.IsExpression;
import com.butent.bee.server.sql.SqlSelect;
import com.butent.bee.server.sql.SqlUtils;
import com.butent.bee.shared.data.BeeRowSet;
import com.butent.bee.shared.data.filter.Filter;
import com.butent.bee.shared.data.filter.Operator;
import com.butent.bee.shared.utils.BeeUtils;
import com.butent.bee.shared.utils.Codec;

import java.util.Arrays;
import java.util.Objects;

import javax.ejb.EJB;
import javax.ws.rs.GET;
import javax.ws.rs.HeaderParam;
import javax.ws.rs.NotFoundException;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

@Path("/")
public class Worker {

  @EJB
  QueryServiceBean qs;
  @EJB
  SystemBean sys;
  @EJB
  FileStorageBean fs;

  @GET
  @Path("users")
  @Produces(MediaType.APPLICATION_JSON)
  public Response getUsers(@HeaderParam(LAST_SYNC_TIME) Long lastSynced) {
    long time = System.currentTimeMillis();

    BeeRowSet users = qs.getViewData(TBL_USERS, Objects.isNull(lastSynced) ? null
            : Filter.compareVersion(Operator.GT, lastSynced), null,
        Arrays.asList(COL_COMPANY_PERSON, COL_USER_BLOCK_BEFORE, COL_USER_BLOCK_AFTER));

    Response response = rowSetResponse(users);
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
}
