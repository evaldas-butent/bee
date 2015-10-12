package com.butent.bee.server.rest;

import static com.butent.bee.shared.modules.classifiers.ClassifierConstants.*;

import com.butent.bee.server.data.QueryServiceBean;
import com.butent.bee.server.data.SystemBean;
import com.butent.bee.server.sql.SqlSelect;
import com.butent.bee.shared.BeeConst;
import com.butent.bee.shared.data.SimpleRowSet;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.ejb.EJB;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

@Path("/")
public class Worker {

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
