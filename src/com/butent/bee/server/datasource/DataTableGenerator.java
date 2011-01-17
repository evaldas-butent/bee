package com.butent.bee.server.datasource;

import com.butent.bee.server.datasource.query.Query;
import com.butent.bee.shared.data.DataException;
import com.butent.bee.shared.data.DataTable;

import javax.servlet.http.HttpServletRequest;

public interface DataTableGenerator {
  DataTable generateDataTable(Query query, HttpServletRequest request) throws DataException;
  Capabilities getCapabilities();
}
