package com.butent.bee.egg.server.datasource.query.engine;

import com.google.common.collect.Sets;

import com.butent.bee.egg.server.datasource.datatable.DataTable;
import com.butent.bee.egg.shared.data.value.Value;

import java.util.Map;
import java.util.Set;

public class AggregationTree {
  private static AggregationPath getPathToNode(AggregationNode node) {
    AggregationPath result = new AggregationPath();
    AggregationNode curNode = node;
    while (curNode.getValue() != null) {
      result.add(curNode.getValue());
      curNode = curNode.getParent();
    }
    result.reverse();
    return result;
  }
  private AggregationNode root;
  private Set<String> columnsToAggregate;

  private DataTable table;

  public AggregationTree(Set<String> columnsToAggregate, DataTable table) {
    this.columnsToAggregate = columnsToAggregate;
    this.table = table;
    root = new AggregationNode(columnsToAggregate, table);
  }

  public void aggregate(AggregationPath path, Map<String, Value> valuesToAggregate) {
    AggregationNode curNode = root;
    root.aggregate(valuesToAggregate);

    for (Value curValue : path.getValues()) {
      if (!curNode.containsChild(curValue)) {
        curNode.addChild(curValue, columnsToAggregate, table);
      }
      curNode = curNode.getChild(curValue);
      curNode.aggregate(valuesToAggregate);
    }
  }

  public AggregationNode getNode(AggregationPath path) {
    AggregationNode curNode = root;
    for (Value curValue : path.getValues()) {
      curNode = curNode.getChild(curValue);
    }
    return curNode;
  }

  public Set<AggregationPath> getPathsToLeaves() {
    Set<AggregationPath> result = Sets.newHashSet();
    getPathsToLeavesInternal(root, result);
    return result;
  }

  private void getPathsToLeavesInternal(AggregationNode node, Set<AggregationPath> result) {
    Map<Value, AggregationNode> children = node.getChildren();
    if (children.isEmpty()) {
      result.add(getPathToNode(node));
    } else {
      for (AggregationNode curNode : children.values()) {
        getPathsToLeavesInternal(curNode, result);
      }
    }
  }
}
