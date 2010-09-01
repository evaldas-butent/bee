package com.butent.bee.egg.server.jdbc;

import com.butent.bee.egg.shared.exceptions.BeeException;

@SuppressWarnings("serial")
public class JdbcWarning extends BeeException {

  public JdbcWarning() {
    super();
  }

  public JdbcWarning(String message, Throwable cause) {
    super(message, cause);
  }

  public JdbcWarning(String message) {
    super(message);
  }

  public JdbcWarning(Throwable cause) {
    super(cause);
  }

}
