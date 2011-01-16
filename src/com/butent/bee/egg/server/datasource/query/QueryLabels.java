package com.butent.bee.egg.server.datasource.query;

import com.google.common.collect.ImmutableSet;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;

import com.butent.bee.egg.shared.data.InvalidQueryException;
import com.butent.bee.egg.shared.data.column.AbstractColumn;
import com.butent.bee.egg.shared.data.column.AggregationColumn;
import com.butent.bee.egg.shared.data.column.ScalarFunctionColumn;
import com.butent.bee.egg.shared.utils.BeeUtils;
import com.butent.bee.egg.shared.utils.LogUtils;

import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.logging.Logger;

public class QueryLabels {
  private static final Logger logger = Logger.getLogger(QueryLabels.class.getName());

  private Map<AbstractColumn, String> columnLabels;

  public QueryLabels() {
    columnLabels = Maps.newHashMap();
  }

  public void addLabel(AbstractColumn column, String label) throws InvalidQueryException {
    if (columnLabels.keySet().contains(column)) {
      String messageToLogAndUser = "Column [" + column.toString() + "] is "
          + "specified more than once in LABEL.";
      LogUtils.severe(logger, messageToLogAndUser);
      throw new InvalidQueryException(messageToLogAndUser);
    }
    columnLabels.put(column, label);
  }

  @Override
  public boolean equals(Object obj) {
    if (this == obj) {
      return true;
    }
    if (obj == null) {
      return false;
    }
    if (getClass() != obj.getClass()) {
      return false;
    }
    QueryLabels other = (QueryLabels) obj;
    if (columnLabels == null) {
      if (other.columnLabels != null) {
        return false;
      }
    } else if (!columnLabels.equals(other.columnLabels)) {
      return false;
    }
    return true;
  }

  public List<AggregationColumn> getAggregationColumns() {
    List<AggregationColumn> result = Lists.newArrayList();
    for (AbstractColumn col : columnLabels.keySet()) {
      result.addAll(col.getAllAggregationColumns());
    }
    return result;
  }

  public Set<AbstractColumn> getColumns() {
    return ImmutableSet.copyOf(columnLabels.keySet());
  }
  
  public String getLabel(AbstractColumn column) {
    return columnLabels.get(column);
  }

  public List<ScalarFunctionColumn> getScalarFunctionColumns() {
    List<ScalarFunctionColumn> result = Lists.newArrayList();
    for (AbstractColumn col : columnLabels.keySet()) {
      for (ScalarFunctionColumn innerCol : col.getAllScalarFunctionColumns()) {
        if (!result.contains(innerCol)) {
          result.add(innerCol);
        }
      }
    }
    return result;
  }

  @Override
  public int hashCode() {
    final int prime = 31;
    int result = 1;
    result = prime * result + ((columnLabels == null) ? 0 : columnLabels.hashCode());
    return result;
  }
  
  public String toQueryString() {
    StringBuilder builder = new StringBuilder();
    List<String> stringList = Lists.newArrayList();
    for (AbstractColumn col : columnLabels.keySet()) {
      String label = columnLabels.get(col);
      stringList.add(col.toQueryString() + " " + Query.stringToQueryStringLiteral(label));
    }
    BeeUtils.append(builder, stringList, ", ");
    return builder.toString(); 
  }
}
