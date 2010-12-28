package com.butent.bee.egg.server.datasource.query.engine;

import com.google.common.collect.Maps;

import com.butent.bee.egg.server.datasource.datatable.DataTable;
import com.butent.bee.egg.server.datasource.datatable.TableRow;
import com.butent.bee.egg.server.datasource.datatable.value.Value;
import com.butent.bee.egg.server.datasource.query.AggregationType;

import java.util.List;
import java.util.Map;
import java.util.Set;

public class TableAggregator {
  private List<String> groupByColumns;
  private Set<String> aggregateColumns;
  private AggregationTree tree;

  public TableAggregator(List<String> groupByColumns, Set<String> aggregateColumns,
      DataTable table) {

    this.groupByColumns = groupByColumns;
    this.aggregateColumns = aggregateColumns;

    tree = new AggregationTree(aggregateColumns, table);

    for (TableRow row : table.getRows()) {
      tree.aggregate(getRowPath(row, table, groupByColumns.size() - 1), 
          getValuesToAggregate(row, table));
    }
  }

  public Value getAggregationValue(AggregationPath path, String columnId,
      AggregationType type) {
    return tree.getNode(path).getAggregationValue(columnId, type);
  }

  public Set<AggregationPath> getPathsToLeaves() {
    return tree.getPathsToLeaves();
  }

  public AggregationPath getRowPath(TableRow row, DataTable table, int depth) {
    AggregationPath result = new AggregationPath();
    for (int i = 0; i <= depth; i++) {
      String columnId = groupByColumns.get(i);
      Value curValue = row.getCell(table.getColumnIndex(columnId)).getValue();
      result.add(curValue);
    }
    return result;
  }

  private Map<String, Value> getValuesToAggregate(TableRow row, DataTable table) {
    Map<String, Value> result = Maps.newHashMap();
    for (String columnId : aggregateColumns) {
      Value curValue = row.getCell(table.getColumnIndex(columnId)).getValue();
      result.put(columnId, curValue);
    }
    return result;
  }
}
