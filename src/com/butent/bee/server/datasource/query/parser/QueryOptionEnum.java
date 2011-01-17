package com.butent.bee.server.datasource.query.parser;

import com.butent.bee.server.datasource.query.QueryOptions;

enum QueryOptionEnum { NO_VALUES, NO_FORMAT;
  public void setInQueryOptions(QueryOptions queryOptions) {
    switch (this) {
      case NO_VALUES:
        queryOptions.setNoValues(true);
        break;
      case NO_FORMAT:
        queryOptions.setNoFormat(true);
        break;
    }
  }
}
