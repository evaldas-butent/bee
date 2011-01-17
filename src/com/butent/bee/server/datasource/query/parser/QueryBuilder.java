package com.butent.bee.server.datasource.query.parser;

import com.butent.bee.server.datasource.query.Query;
import com.butent.bee.shared.data.InvalidQueryException;
import com.butent.bee.shared.data.Messages;
import com.butent.bee.shared.utils.BeeUtils;
import com.butent.bee.shared.utils.LogUtils;

import java.util.logging.Logger;

public class QueryBuilder {

  private static final Logger logger = Logger.getLogger(QueryBuilder.class.getName());

  private static final QueryBuilder SINGLETON = new QueryBuilder();

  public static QueryBuilder getInstance() {
    return SINGLETON;
  }

  private QueryBuilder() {
  }

  public Query parseQuery(String tqValue) throws InvalidQueryException {
    Query query;
    if (BeeUtils.isEmpty(tqValue)) {
      query = new Query();
    } else {
      try {
        query = QueryParser.parseString(tqValue);
      } catch (ParseException ex) {
        String messageToUserAndLog = ex.getMessage();
        LogUtils.severe(logger, "Parsing error: " + messageToUserAndLog);
        throw new InvalidQueryException(Messages.PARSE_ERROR.getMessage(messageToUserAndLog));
      }
      query.validate();
    }
    return query;
  }
}
