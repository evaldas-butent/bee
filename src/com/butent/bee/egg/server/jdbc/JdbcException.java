package com.butent.bee.egg.server.jdbc;

import com.butent.bee.egg.shared.exceptions.BeeException;

@SuppressWarnings("serial")
public class JdbcException extends BeeException {

  public JdbcException() {
    super();
  }

  public JdbcException(String message, Throwable cause) {
    super(message, cause);
  }

  public JdbcException(String message) {
    super(message);
  }

  public JdbcException(Throwable cause) {
    super(cause);
  }

}
