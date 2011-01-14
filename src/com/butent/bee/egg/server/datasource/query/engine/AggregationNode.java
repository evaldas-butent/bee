package com.butent.bee.egg.server.datasource.query.engine;

import com.google.common.collect.Maps;

import com.butent.bee.egg.server.datasource.datatable.DataTable;
import com.butent.bee.egg.server.datasource.query.AggregationType;
import com.butent.bee.egg.shared.data.value.Value;

import java.util.Map;
import java.util.NoSuchElementException;
import java.util.Set;

public class AggregationNode {
  private AggregationNode parent;
  private Value value;

  private Map<String, ValueAggregator> columnAggregators = Maps.newHashMap();
  private Map<Value, AggregationNode> children = Maps.newHashMap();

  public AggregationNode(Set<String> columnsToAggregate, DataTable table) {
    for (String columnId : columnsToAggregate) {
      columnAggregators.put(columnId, new ValueAggregator(
          table.getColumnDescription(columnId).getType()));
    }
  }

  public void addChild(Value key, Set<String> columnsToAggregate, DataTable table) {
    if (children.containsKey(key)) {
      throw new IllegalArgumentException("A child with key: " + key + " already exists.");
    }
    AggregationNode node = new AggregationNode(columnsToAggregate, table);
    node.parent = this;
    node.value = key;
    children.put(key, node);
  }

  public void aggregate(Map<String, Value> valuesByColumn) {
    for (String columnId : valuesByColumn.keySet()) {
      columnAggregators.get(columnId).aggregate(valuesByColumn.get(columnId));
    }
  }

  public boolean containsChild(Value v) {
    return children.containsKey(v);
  }

  public Value getAggregationValue(String columnId, AggregationType type) {
    ValueAggregator valuesAggregator = columnAggregators.get(columnId);
    if (valuesAggregator == null) {
      throw new IllegalArgumentException("Column " + columnId + " is not aggregated");
    }
    return valuesAggregator.getValue(type);
  }

  public AggregationNode getChild(Value v) {
    AggregationNode result = children.get(v);
    if (result == null) {
      throw new NoSuchElementException("Value " + v + " is not a child.");
    }
    return result;
  }

  public Map<Value, AggregationNode> getChildren() {
    return Maps.newHashMap(children);
  }

  protected AggregationNode getParent() {
    return parent;
  }

  protected Value getValue() {
    return value;
  }
}
