package com.butent.bee.egg.server.datasource.query;

import com.google.common.collect.Lists;

import com.butent.bee.egg.server.datasource.base.InvalidQueryException;
import com.butent.bee.egg.server.datasource.datatable.DataTable;
import com.butent.bee.egg.server.datasource.datatable.TableCell;
import com.butent.bee.egg.server.datasource.datatable.TableRow;
import com.butent.bee.egg.server.datasource.query.scalarfunction.ScalarFunction;
import com.butent.bee.egg.shared.data.value.Value;
import com.butent.bee.egg.shared.data.value.ValueType;
import com.butent.bee.egg.shared.utils.BeeUtils;

import java.util.List;

public class ScalarFunctionColumn extends AbstractColumn {
  public static final String COLUMN_FUNCTION_TYPE_SEPARATOR = "_";
  public static final String COLUMN_COLUMN_SEPARATOR = ",";

  private List<AbstractColumn> columns;
  private ScalarFunction scalarFunction;

  public ScalarFunctionColumn(List<AbstractColumn> columns, ScalarFunction scalarFunction) {
    this.columns = columns;
    this.scalarFunction = scalarFunction;
  }

  @Override
  public boolean equals(Object o) {
    if (o instanceof ScalarFunctionColumn) {
      ScalarFunctionColumn other = (ScalarFunctionColumn) o;
      return columns.equals(other.columns) && scalarFunction.equals(other.scalarFunction);
    }
    return false;
  }

  @Override
  public List<AggregationColumn> getAllAggregationColumns() {
    List<AggregationColumn> aggregationColumns = Lists.newArrayListWithCapacity(columns.size());
    for (AbstractColumn column : columns) {
      aggregationColumns.addAll(column.getAllAggregationColumns());
    }
    return aggregationColumns;
  }

  @Override
  public List<ScalarFunctionColumn> getAllScalarFunctionColumns() {
    List<ScalarFunctionColumn> scalarFunctionColumns = Lists.newArrayList(this);
    for (AbstractColumn column : columns) {
      scalarFunctionColumns.addAll(column.getAllScalarFunctionColumns());
    }
    return scalarFunctionColumns;
  }

  @Override
  public List<String> getAllSimpleColumnIds() {
    List<String> columnIds = Lists.newArrayList();
    for (AbstractColumn column : columns) {
      columnIds.addAll(column.getAllSimpleColumnIds());
    }
    return columnIds;
  }

  @Override
  public List<SimpleColumn> getAllSimpleColumns() {
    List<SimpleColumn> simpleColumns = Lists.newArrayListWithCapacity(columns.size());
    for (AbstractColumn column : columns) {
      simpleColumns.addAll(column.getAllSimpleColumns());
    }
    return simpleColumns;
  }

  @Override
  public TableCell getCell(ColumnLookup lookup, TableRow row) {
    if (lookup.containsColumn(this)) {
      int columnIndex = lookup.getColumnIndex(this);
      return row.getCells().get(columnIndex);
    }

    List<Value> functionParameters = Lists.newArrayListWithCapacity(columns.size());
    for (AbstractColumn column : columns) {
      functionParameters.add(column.getValue(lookup, row));
    }
    return new TableCell(scalarFunction.evaluate(functionParameters));
  }

  public List<AbstractColumn> getColumns() {
    return columns;
  }

  public ScalarFunction getFunction() {
    return scalarFunction;
  }

  @Override
  public String getId() {
    List<String> colIds = Lists.newArrayList();
    for (AbstractColumn col : columns) {
      colIds.add(col.getId());
    }
    return BeeUtils.append(new StringBuilder(scalarFunction.getFunctionName())
        .append(COLUMN_FUNCTION_TYPE_SEPARATOR), colIds, COLUMN_COLUMN_SEPARATOR).toString();
  }

  @Override
  public ValueType getValueType(DataTable dataTable) {
    if (dataTable.containsColumn(this.getId())) {
      return dataTable.getColumnDescription(this.getId()).getType();
    }
    List<ValueType> types = Lists.newArrayListWithCapacity(columns.size());
    for (AbstractColumn column : columns) {
      types.add(column.getValueType(dataTable));
    }
    return scalarFunction.getReturnType(types);
  }

  @Override
  public int hashCode() {
    int hash  = 1279;
    for (AbstractColumn column : columns) {
      hash = (hash * 17) + column.hashCode();
    }
    hash = (hash * 17) + scalarFunction.hashCode();
    return hash;
  }

  @Override
  public String toQueryString() {
    List<String> columnQueryStrings = Lists.newArrayList();
    for (AbstractColumn column : columns) {
      columnQueryStrings.add(column.toQueryString());
    }
    return scalarFunction.toQueryString(columnQueryStrings);
  }

  @Override
  public String toString() {
    List<String> colNames = Lists.newArrayList();
    for (AbstractColumn col : columns) {
      colNames.add(col.toString());
    }
    return BeeUtils.append(new StringBuilder(scalarFunction.getFunctionName()).append("("),
        colNames, COLUMN_COLUMN_SEPARATOR).append(")").toString();
  }

  @Override
  public void validateColumn(DataTable dataTable) throws InvalidQueryException {
    List<ValueType> types = Lists.newArrayListWithCapacity(columns.size());
    for (AbstractColumn column : columns) {
      column.validateColumn(dataTable);
      types.add(column.getValueType(dataTable));
    }
    scalarFunction.validateParameters(types);
  }
}
