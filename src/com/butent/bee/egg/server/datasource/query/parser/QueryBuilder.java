package com.butent.bee.egg.server.datasource.query.parser;

import com.butent.bee.egg.server.datasource.base.InvalidQueryException;
import com.butent.bee.egg.server.datasource.base.MessagesEnum;
import com.butent.bee.egg.server.datasource.query.Query;
import com.butent.bee.egg.shared.utils.BeeUtils;
import com.butent.bee.egg.shared.utils.LogUtils;

import com.ibm.icu.util.ULocale;

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
    return parseQuery(tqValue, null);
  }
  
  public Query parseQuery(String tqValue, ULocale ulocale) throws InvalidQueryException {
    Query query;
    if (BeeUtils.isEmpty(tqValue)) {
      query = new Query();
    } else {
      try {
        query = QueryParser.parseString(tqValue);
      } catch (ParseException ex) {
        String messageToUserAndLog = ex.getMessage();
        LogUtils.severe(logger, "Parsing error: " + messageToUserAndLog);
        throw new InvalidQueryException(MessagesEnum.PARSE_ERROR.getMessageWithArgs(ulocale, 
            messageToUserAndLog));
      }
      query.setLocaleForUserMessages(ulocale);
      query.validate();
    }
    return query;
  }
}
