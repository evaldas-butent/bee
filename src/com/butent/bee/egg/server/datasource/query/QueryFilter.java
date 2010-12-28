package com.butent.bee.egg.server.datasource.query;

import com.butent.bee.egg.server.datasource.datatable.DataTable;
import com.butent.bee.egg.server.datasource.datatable.TableRow;

import java.util.List;
import java.util.Set;

public abstract class QueryFilter {
  public abstract Set<String> getAllColumnIds();

  public abstract List<ScalarFunctionColumn> getScalarFunctionColumns();

  public abstract boolean isMatch(DataTable table, TableRow row);
  
  public abstract String toQueryString();

  protected abstract List<AggregationColumn> getAggregationColumns();
}
