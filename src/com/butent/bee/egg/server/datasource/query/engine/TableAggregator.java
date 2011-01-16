package com.butent.bee.egg.server.datasource.query.engine;

import com.google.common.collect.Maps;

import com.butent.bee.egg.shared.data.Aggregation;
import com.butent.bee.egg.shared.data.IsRow;
import com.butent.bee.egg.shared.data.IsTable;
import com.butent.bee.egg.shared.data.value.Value;

import java.util.List;
import java.util.Map;
import java.util.Set;

public class TableAggregator {
  private List<String> groupByColumns;
  private Set<String> aggregateColumns;
  private AggregationTree tree;

  public TableAggregator(List<String> groupByColumns, Set<String> aggregateColumns,
      IsTable table) {

    this.groupByColumns = groupByColumns;
    this.aggregateColumns = aggregateColumns;

    tree = new AggregationTree(aggregateColumns, table);

    for (IsRow row : table.getRows()) {
      tree.aggregate(getRowPath(row, table, groupByColumns.size() - 1), 
          getValuesToAggregate(row, table));
    }
  }

  public Value getAggregationValue(AggregationPath path, String columnId, Aggregation type) {
    return tree.getNode(path).getAggregationValue(columnId, type);
  }

  public Set<AggregationPath> getPathsToLeaves() {
    return tree.getPathsToLeaves();
  }

  public AggregationPath getRowPath(IsRow row, IsTable table, int depth) {
    AggregationPath result = new AggregationPath();
    for (int i = 0; i <= depth; i++) {
      String columnId = groupByColumns.get(i);
      Value curValue = row.getCell(table.getColumnIndex(columnId)).getValue();
      result.add(curValue);
    }
    return result;
  }

  private Map<String, Value> getValuesToAggregate(IsRow row, IsTable table) {
    Map<String, Value> result = Maps.newHashMap();
    for (String columnId : aggregateColumns) {
      Value curValue = row.getCell(table.getColumnIndex(columnId)).getValue();
      result.put(columnId, curValue);
    }
    return result;
  }
}
