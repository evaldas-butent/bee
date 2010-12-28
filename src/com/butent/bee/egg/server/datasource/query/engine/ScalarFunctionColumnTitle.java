package com.butent.bee.egg.server.datasource.query.engine;

import com.butent.bee.egg.server.datasource.datatable.ColumnDescription;
import com.butent.bee.egg.server.datasource.datatable.DataTable;
import com.butent.bee.egg.server.datasource.datatable.value.Value;
import com.butent.bee.egg.server.datasource.datatable.value.ValueType;
import com.butent.bee.egg.server.datasource.query.AbstractColumn;
import com.butent.bee.egg.server.datasource.query.AggregationColumn;
import com.butent.bee.egg.server.datasource.query.ScalarFunctionColumn;
import com.butent.bee.egg.shared.utils.BeeUtils;

import java.util.List;

class ScalarFunctionColumnTitle {
  public static final String PIVOT_COLUMNS_SEPARATOR = ",";
  public static final String PIVOT_SCALAR_FUNCTION_SEPARATOR = " ";

  public static String getColumnDescriptionLabel(DataTable originalTable, AbstractColumn column) {
    StringBuilder label = new StringBuilder();
    if (originalTable.containsColumn(column.getId())) {
      label.append(originalTable.getColumnDescription(column.getId()).getLabel());
    } else {
      if (column instanceof AggregationColumn) {
        AggregationColumn aggColumn = (AggregationColumn) column;
        label.append(aggColumn.getAggregationType().getCode()).append(" ").append(
            originalTable.getColumnDescription(aggColumn.getAggregatedColumn().getId()).getLabel());
      } else {
        ScalarFunctionColumn scalarFunctionColumn = (ScalarFunctionColumn) column;
        List<AbstractColumn> columns = scalarFunctionColumn.getColumns();
        label.append(scalarFunctionColumn.getFunction().getFunctionName()).append("(");
        for (AbstractColumn abstractColumn : columns) {
          label.append(getColumnDescriptionLabel(originalTable, abstractColumn));
        }
        label.append(")");
      }
    }
    return label.toString();
  }

  public ScalarFunctionColumn scalarFunctionColumn;

  private List<Value> values;

  public ScalarFunctionColumnTitle(List<Value> values, ScalarFunctionColumn column) {
    this.values = values;
    this.scalarFunctionColumn = column;
  }

  public ColumnDescription createColumnDescription(DataTable originalTable) {
    String columnId = createIdPivotPrefix() + scalarFunctionColumn.getId();
    ValueType type = scalarFunctionColumn.getValueType(originalTable);
    String label = createLabelPivotPart() + " " +
        getColumnDescriptionLabel(originalTable, scalarFunctionColumn);
    ColumnDescription result = new ColumnDescription(columnId, type, label);

    return result;
  }

  @Override
  public boolean equals(Object o) {
    if (o instanceof ScalarFunctionColumnTitle) {
      ScalarFunctionColumnTitle other = (ScalarFunctionColumnTitle) o;
      return (values.equals(other.values) && scalarFunctionColumn.equals(other.scalarFunctionColumn));
    }
    return false;
  }

  public List<Value> getValues() {
    return values;
  }

  @Override
  public int hashCode() {
    int result = 31;
    if (scalarFunctionColumn != null) {
      result += scalarFunctionColumn.hashCode();
    }
    result *= 31;
    if (values != null) {
      result += values.hashCode();
    }
    return result;
  }

  private String createIdPivotPrefix() {
    if (!isPivot()) {
      return "";
    }
    return BeeUtils.append(new StringBuilder(), values, PIVOT_COLUMNS_SEPARATOR)
        .append(PIVOT_SCALAR_FUNCTION_SEPARATOR).toString();
  }

  private String createLabelPivotPart() {
    if (!isPivot()) {
      return "";
    }
    return BeeUtils.append(new StringBuilder(), values, PIVOT_COLUMNS_SEPARATOR).toString();
  }

  private boolean isPivot() {
    return (!values.isEmpty());
  }
}
