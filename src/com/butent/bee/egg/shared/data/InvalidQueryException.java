package com.butent.bee.egg.shared.data;

@SuppressWarnings("serial")
public class InvalidQueryException extends DataException {
  public InvalidQueryException(String messageToUser) {
    super(Reasons.INVALID_QUERY, messageToUser);
  }
}
