package com.butent.bee.egg.server.datasource.query.engine;

import com.butent.bee.egg.server.datasource.datatable.ColumnDescription;
import com.butent.bee.egg.server.datasource.datatable.DataTable;
import com.butent.bee.egg.server.datasource.query.AggregationColumn;
import com.butent.bee.egg.server.datasource.query.AggregationType;
import com.butent.bee.egg.shared.Assert;
import com.butent.bee.egg.shared.data.value.Value;
import com.butent.bee.egg.shared.data.value.ValueType;
import com.butent.bee.egg.shared.utils.BeeUtils;

import java.util.List;

class ColumnTitle {
  public static final String PIVOT_COLUMNS_SEPARATOR = ",";
  public static final String PIVOT_AGGREGATION_SEPARATOR = " ";

  public AggregationColumn aggregation;

  private List<Value> values;
  private boolean isMultiAggregationQuery;

  public ColumnTitle(List<Value> values,
      AggregationColumn aggregationColumn, boolean isMultiAggregationQuery) {
    this.values = values;
    this.aggregation = aggregationColumn;
    this.isMultiAggregationQuery = isMultiAggregationQuery;
  }

  public ColumnDescription createColumnDescription(DataTable originalTable) {
    ColumnDescription colDesc = originalTable.getColumnDescription(
        aggregation.getAggregatedColumn().getId());
    return createAggregationColumnDescription(colDesc);
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) {
      return true;
    }
    if (!(o instanceof ColumnTitle)) {
      return false;
    }
    ColumnTitle other = (ColumnTitle) o;
    return values.equals(other.values) && aggregation.equals(other.aggregation);
  }

  public List<Value> getValues() {
    return values;
  }

  @Override
  public int hashCode() {
    int hash  = 1279;
    hash = (hash * 17) + values.hashCode();
    hash = (hash * 17) + aggregation.hashCode();
    return hash;
  }

  ColumnDescription createAggregationColumnDescription(
      ColumnDescription originalColumnDescription) {
    AggregationType aggregationType = aggregation.getAggregationType();
    String columnId = createIdPivotPrefix() + aggregation.getId();
    ValueType type = originalColumnDescription.getType();
    String aggregationLabelPart = aggregation.getAggregationType().getCode()
        + " " + originalColumnDescription.getLabel();
    String pivotLabelPart = createLabelPivotPart();
    String label;
    if (isPivot()) {
      if (isMultiAggregationQuery) {
        label = pivotLabelPart + " " + aggregationLabelPart;
      } else {
        label = pivotLabelPart;
      }
    } else {
      label = aggregationLabelPart;
    }

    ColumnDescription result;
    if (canUseSameTypeForAggregation(type, aggregationType)) {
      result = new ColumnDescription(columnId, type, label);
    } else {
      result = new ColumnDescription(columnId, ValueType.NUMBER, label);
    }

    return result;
  }

  private boolean canUseSameTypeForAggregation(ValueType valueType,
      AggregationType aggregationType) {
    boolean ans;
    if (valueType == ValueType.NUMBER) {
      ans = true;
    } else {
      switch (aggregationType) {
        case MIN:
        case MAX:
          ans = true;
          break;
        case SUM:
        case AVG:
        case COUNT:
          ans = false;
          break;
        default:
          ans = false;
          Assert.untouchable();
      }
    }
    return ans;
  }

  private String createIdPivotPrefix() {
    if (!isPivot()) {
      return "";
    }
    return BeeUtils.append(new StringBuilder(), values, PIVOT_COLUMNS_SEPARATOR)
        .append(PIVOT_AGGREGATION_SEPARATOR).toString();
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
