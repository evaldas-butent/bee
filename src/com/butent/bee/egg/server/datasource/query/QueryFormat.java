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

public class QueryFormat {
  private static final Logger logger = Logger.getLogger(QueryFormat.class.getName());

  private Map<AbstractColumn, String> columnPatterns;

  public QueryFormat() {
    columnPatterns = Maps.newHashMap();
  }

  public void addPattern(AbstractColumn column, String pattern) throws InvalidQueryException {
    if (columnPatterns.keySet().contains(column)) {
      String messageToLogAndUser = "Column [" + column.toString() + "] is "
          + "specified more than once in FORMAT.";
      LogUtils.severe(logger, messageToLogAndUser);
      throw new InvalidQueryException(messageToLogAndUser);
    }
    columnPatterns.put(column, pattern);
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
    QueryFormat other = (QueryFormat) obj;
    if (columnPatterns == null) {
      if (other.columnPatterns != null) {
        return false;
      }
    } else if (!columnPatterns.equals(other.columnPatterns)) {
      return false;
    }
    return true;
  }

  public List<AggregationColumn> getAggregationColumns() {
    List<AggregationColumn> result = Lists.newArrayList();
    for (AbstractColumn col : columnPatterns.keySet()) {
      result.addAll(col.getAllAggregationColumns());
    }
    return result;
  }

  public Set<AbstractColumn> getColumns() {
    return ImmutableSet.copyOf(columnPatterns.keySet());
  }

  public String getPattern(AbstractColumn column) {
    return columnPatterns.get(column);
  }
  
  public List<ScalarFunctionColumn> getScalarFunctionColumns() {
    List<ScalarFunctionColumn> result = Lists.newArrayList();
    for (AbstractColumn col : columnPatterns.keySet()) {
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
    result = prime * result + ((columnPatterns == null) ? 0 : columnPatterns.hashCode());
    return result;
  }
  
  public String toQueryString() {
    StringBuilder builder = new StringBuilder();
    List<String> stringList = Lists.newArrayList();
    for (AbstractColumn col : columnPatterns.keySet()) {
      String pattern = columnPatterns.get(col);
      stringList.add(col.toQueryString() + " " + Query.stringToQueryStringLiteral(pattern));
    }
    BeeUtils.append(builder, stringList, ", ");
    return builder.toString(); 
  }
}
