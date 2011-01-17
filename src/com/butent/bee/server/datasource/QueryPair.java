package com.butent.bee.server.datasource;

import com.butent.bee.server.datasource.query.Query;

public class QueryPair {
  private Query dataSourceQuery;
  private Query completionQuery;
  
  public QueryPair(Query dataSourceQuery, Query completionQuery) {
    this.dataSourceQuery = dataSourceQuery;
    this.completionQuery = completionQuery;
  }
  
  public Query getCompletionQuery() {
    return completionQuery;   
  }

  public Query getDataSourceQuery() {
    return dataSourceQuery;
  }
}
