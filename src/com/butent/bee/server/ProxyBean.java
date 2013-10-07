package com.butent.bee.server;

import com.butent.bee.server.data.QueryServiceBean;
import com.butent.bee.server.sql.SqlInsert;
import com.butent.bee.shared.communication.ResponseObject;

import javax.ejb.EJB;
import javax.ejb.LocalBean;
import javax.ejb.Stateless;

@Stateless
@LocalBean
public class ProxyBean {

  @EJB
  QueryServiceBean queryService;

  public ResponseObject insert(SqlInsert si) {
    return queryService.insertDataWithResponse(si);
  }
}
