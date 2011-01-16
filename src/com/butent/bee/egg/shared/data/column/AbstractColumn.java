package com.butent.bee.egg.shared.data.column;

import com.butent.bee.egg.shared.data.DataTable;
import com.butent.bee.egg.shared.data.InvalidQueryException;
import com.butent.bee.egg.shared.data.IsCell;
import com.butent.bee.egg.shared.data.IsRow;
import com.butent.bee.egg.shared.data.value.Value;
import com.butent.bee.egg.shared.data.value.ValueType;

import java.util.List;

public abstract class AbstractColumn {

  @Override
  public abstract boolean equals(Object o);

  public abstract List<AggregationColumn> getAllAggregationColumns();

  public abstract List<ScalarFunctionColumn> getAllScalarFunctionColumns();
  
  public abstract List<String> getAllSimpleColumnIds();

  public abstract List<SimpleColumn> getAllSimpleColumns();

  public IsCell getCell(ColumnLookup lookup, IsRow row) {
    int columnIndex = lookup.getColumnIndex(this);
    return row.getCells().get(columnIndex);
  }

  public abstract String getId();

  public Value getValue(ColumnLookup lookup, IsRow row) {
    return getCell(lookup, row).getValue();
  }

  public abstract ValueType getValueType(DataTable dataTable);

  @Override
  public abstract int hashCode();

  public abstract String toQueryString();

  public abstract void validateColumn(DataTable dataTable) throws InvalidQueryException;
}
