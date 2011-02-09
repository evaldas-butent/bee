package com.butent.bee.server.datasource;

import com.butent.bee.server.datasource.query.Query;
import com.butent.bee.shared.data.DataException;
import com.butent.bee.shared.data.IsTable;

import javax.servlet.http.HttpServletRequest;

public interface DataTableGenerator {
  IsTable<?, ?> generateDataTable(Query query, HttpServletRequest request) throws DataException;
  Capabilities getCapabilities();
}
