package com.butent.bee.egg.shared.data.filter;

import com.butent.bee.egg.shared.data.IsRow;
import com.butent.bee.egg.shared.data.IsTable;
import com.butent.bee.egg.shared.data.column.AggregationColumn;
import com.butent.bee.egg.shared.data.column.ScalarFunctionColumn;

import java.util.List;
import java.util.Set;

public abstract class RowFilter {
  public abstract List<AggregationColumn> getAggregationColumns();

  public abstract Set<String> getAllColumnIds();

  public abstract List<ScalarFunctionColumn> getScalarFunctionColumns();
  
  public abstract boolean isMatch(IsTable table, IsRow row);

  public abstract String toQueryString();
}
