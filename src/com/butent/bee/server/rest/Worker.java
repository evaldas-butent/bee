package com.butent.bee.server.rest;

import static com.butent.bee.shared.modules.classifiers.ClassifierConstants.*;

import com.butent.bee.server.data.QueryServiceBean;
import com.butent.bee.server.data.SystemBean;
import com.butent.bee.server.data.UserServiceBean;
import com.butent.bee.server.sql.SqlSelect;
import com.butent.bee.shared.BeeConst;
import com.butent.bee.shared.data.SimpleRowSet;
import com.butent.bee.shared.utils.BeeUtils;

import java.util.HashMap;
import java.util.Map;

import javax.ejb.EJB;
import javax.ws.rs.GET;
import javax.ws.rs.HeaderParam;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

@Path("/")
public class Worker {

  @EJB
  UserServiceBean usr;
  @EJB
  QueryServiceBean qs;
  @EJB
  SystemBean sys;

  @GET
  @Path("companies")
  @Produces(MediaType.APPLICATION_JSON)
  public Response companies() {
    SimpleRowSet companies = qs.getData(new SqlSelect().setLimit(10)
        .addFields(TBL_COMPANIES, sys.getIdName(TBL_COMPANIES), COL_COMPANY_NAME, COL_COMPANY_CODE)
        .addFrom(TBL_COMPANIES));

    return rowSetResponse(companies);
  }

  @GET
  @Path(EntryPoint.ENTRY)
  public String entry(@HeaderParam("licence") String licence) {
    return BeeUtils.joinWords("EJB:", usr.getCurrentUser());
  }

  private Response rowSetResponse(SimpleRowSet rowSet) {
    Map<String, Object> map = new HashMap<>();
    map.put("columns", rowSet.getColumnNames());
    map.put("data", rowSet.getRows());

    return Response.ok(map, MediaType.APPLICATION_JSON_TYPE.withCharset(BeeConst.CHARSET_UTF8))
        .build();
  }
}
