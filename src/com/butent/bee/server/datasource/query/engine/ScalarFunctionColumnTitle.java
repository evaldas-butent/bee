package com.butent.bee.server.datasource.query.engine;

import com.butent.bee.shared.data.IsColumn;
import com.butent.bee.shared.data.IsTable;
import com.butent.bee.shared.data.column.AbstractColumn;
import com.butent.bee.shared.data.column.AggregationColumn;
import com.butent.bee.shared.data.column.ScalarFunctionColumn;
import com.butent.bee.shared.data.value.Value;
import com.butent.bee.shared.data.value.ValueType;
import com.butent.bee.shared.utils.BeeUtils;

import java.util.List;

class ScalarFunctionColumnTitle {
  public static final String PIVOT_COLUMNS_SEPARATOR = ",";
  public static final String PIVOT_SCALAR_FUNCTION_SEPARATOR = " ";

  public static String getColumnDescriptionLabel(IsTable<?, ?> originalTable,
      AbstractColumn column) {
    StringBuilder label = new StringBuilder();
    if (originalTable.containsColumn(column.getId())) {
      label.append(originalTable.getColumn(column.getId()).getLabel());
    } else {
      if (column instanceof AggregationColumn) {
        AggregationColumn aggColumn = (AggregationColumn) column;
        label.append(aggColumn.getAggregationType().getCode()).append(" ").append(
            originalTable.getColumn(aggColumn.getAggregatedColumn().getId()).getLabel());
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

  public <C extends IsColumn> C createColumnDescription(IsTable<?, C> originalTable) {
    String columnId = createIdPivotPrefix() + scalarFunctionColumn.getId();
    ValueType type = scalarFunctionColumn.getValueType(originalTable);
    String label = createLabelPivotPart() + " " +
        getColumnDescriptionLabel(originalTable, scalarFunctionColumn);
    C result = originalTable.createColumn(type, label, columnId);

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
