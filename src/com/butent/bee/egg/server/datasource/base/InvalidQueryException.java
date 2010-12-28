package com.butent.bee.egg.server.datasource.base;

@SuppressWarnings("serial")
public class InvalidQueryException extends DataSourceException {
  public InvalidQueryException(String messageToUser) {
    super(ReasonType.INVALID_QUERY, messageToUser);
  }
}
