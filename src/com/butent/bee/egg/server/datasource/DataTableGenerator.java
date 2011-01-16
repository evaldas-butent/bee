package com.butent.bee.egg.server.datasource;

import com.butent.bee.egg.server.datasource.query.Query;
import com.butent.bee.egg.shared.data.DataException;
import com.butent.bee.egg.shared.data.DataTable;

import javax.servlet.http.HttpServletRequest;

public interface DataTableGenerator {
  DataTable generateDataTable(Query query, HttpServletRequest request) throws DataException;
  Capabilities getCapabilities();
}
