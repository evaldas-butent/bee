package com.butent.bee.server;

import com.butent.bee.server.data.QueryServiceBean;
import com.butent.bee.server.data.SystemBean;
import com.butent.bee.server.sql.IsQuery;
import com.butent.bee.server.sql.SqlInsert;
import com.butent.bee.shared.communication.ResponseObject;

import javax.ejb.EJB;
import javax.ejb.LocalBean;
import javax.ejb.Stateless;

@Stateless
@LocalBean
public class ProxyBean {

  @EJB
  QueryServiceBean qs;
  @EJB
  SystemBean sys;
  
  public String[] getColumnValues(IsQuery query) {
    return qs.getColumn(query);
  }
  
  public String getIdName(String tblName) {
    return sys.getIdName(tblName);
  }

  public ResponseObject insert(SqlInsert si) {
    return qs.insertDataWithResponse(si);
  }
  
  public boolean isField(String tblName, String fldName) {
    return sys.isTable(tblName) && sys.hasField(tblName, fldName);
  }
}
