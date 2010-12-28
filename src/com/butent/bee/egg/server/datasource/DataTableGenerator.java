package com.butent.bee.egg.server.datasource;

import com.butent.bee.egg.server.datasource.base.DataSourceException;
import com.butent.bee.egg.server.datasource.datatable.DataTable;
import com.butent.bee.egg.server.datasource.query.Query;

import javax.servlet.http.HttpServletRequest;

public interface DataTableGenerator {
  DataTable generateDataTable(Query query, HttpServletRequest request) throws DataSourceException;
  Capabilities getCapabilities();
}
