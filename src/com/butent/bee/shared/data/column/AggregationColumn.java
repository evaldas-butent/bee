package com.butent.bee.shared.data.column;

import com.google.common.collect.Lists;

import com.butent.bee.shared.data.Aggregation;
import com.butent.bee.shared.data.DataTable;
import com.butent.bee.shared.data.InvalidQueryException;
import com.butent.bee.shared.data.Messages;
import com.butent.bee.shared.data.value.ValueType;

import java.util.List;

public class AggregationColumn extends AbstractColumn {
  public static final String COLUMN_AGGRGATION_TYPE_SEPARATOR = "-";

  private SimpleColumn aggregatedColumn;
  private Aggregation aggregationType;

  public AggregationColumn(SimpleColumn aggregatedColumn, Aggregation aggregationType) {
    this.aggregatedColumn = aggregatedColumn;
    this.aggregationType = aggregationType;
  }

  @Override
  public boolean equals(Object o) {
    if (o instanceof AggregationColumn) {
      AggregationColumn other = (AggregationColumn) o;
      return aggregatedColumn.equals(other.aggregatedColumn)
          && aggregationType.equals(other.aggregationType);
    }
    return false;
  }

  public SimpleColumn getAggregatedColumn() {
    return aggregatedColumn;
  }

  public Aggregation getAggregationType() {
    return aggregationType;
  }

  @Override
  public List<AggregationColumn> getAllAggregationColumns() {
    return Lists.newArrayList(this);
  }

  @Override
  public List<ScalarFunctionColumn> getAllScalarFunctionColumns() {
    return Lists.newArrayList();
  }

  @Override
  public List<String> getAllSimpleColumnIds() {
    return Lists.newArrayList(aggregatedColumn.getId());
  }

  @Override
  public List<SimpleColumn> getAllSimpleColumns() {
    return Lists.newArrayList();
  }

  @Override
  public String getId() {
    return aggregationType.getCode() + COLUMN_AGGRGATION_TYPE_SEPARATOR
        + aggregatedColumn.getId();
  }

  @Override
  public ValueType getValueType(DataTable dataTable) {
    ValueType valueType;
    ValueType originalValueType = dataTable.getColumn(aggregatedColumn.getId()).getType();
    switch (aggregationType) {
      case COUNT:
        valueType = ValueType.NUMBER;
        break;
      case AVG: case SUM: case MAX: case MIN:
      valueType = originalValueType;
      break;
      default: throw new RuntimeException(Messages.INVALID_AGG_TYPE.getMessage(aggregationType));
    }
    return valueType;
  }

  @Override
  public int hashCode() {
    int hash  = 1279;
    hash = (hash * 17) + aggregatedColumn.hashCode();
    hash = (hash * 17) + aggregationType.hashCode();
    return hash;
  }

  @Override
  public String toQueryString() {
    return aggregationType.getCode().toUpperCase() + "("
        + aggregatedColumn.toQueryString() + ")";
  }

  @Override
  public String toString() {
    return aggregationType.getCode() + "(" + aggregatedColumn.getId() + ")";
  }

  @Override
  public void validateColumn(DataTable dataTable) throws InvalidQueryException {
    ValueType valueType = dataTable.getColumn(aggregatedColumn.getId()).getType();
    switch (aggregationType) {
      case COUNT: case MAX: case MIN: break;
      case AVG: case SUM:
      if (valueType != ValueType.NUMBER) {
        throw new InvalidQueryException(Messages.AVG_SUM_ONLY_NUMERIC.getMessage());
      }
      break;
      default: throw new RuntimeException(Messages.INVALID_AGG_TYPE.getMessage(aggregationType));
    }
  }
}
