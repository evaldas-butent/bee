package com.butent.bee.shared.data.column;

import com.butent.bee.shared.data.InvalidQueryException;
import com.butent.bee.shared.data.IsCell;
import com.butent.bee.shared.data.IsRow;
import com.butent.bee.shared.data.IsTable;
import com.butent.bee.shared.data.value.Value;
import com.butent.bee.shared.data.value.ValueType;

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

  public abstract ValueType getValueType(IsTable<?, ? > dataTable);

  @Override
  public abstract int hashCode();

  public abstract String toQueryString();

  public abstract void validateColumn(IsTable<?, ?> dataTable) throws InvalidQueryException;
}
