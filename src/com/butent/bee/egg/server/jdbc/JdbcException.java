package com.butent.bee.egg.server.jdbc;

import java.sql.SQLException;

@SuppressWarnings("serial")
public class JdbcException extends SQLException {

  public JdbcException() {
    super();
  }

  public JdbcException(String reason, String sqlState, int vendorCode,
      Throwable cause) {
    super(reason, sqlState, vendorCode, cause);
  }

  public JdbcException(String reason, String SQLState, int vendorCode) {
    super(reason, SQLState, vendorCode);
  }

  public JdbcException(String reason, String sqlState, Throwable cause) {
    super(reason, sqlState, cause);
  }

  public JdbcException(String reason, String SQLState) {
    super(reason, SQLState);
  }

  public JdbcException(String reason, Throwable cause) {
    super(reason, cause);
  }

  public JdbcException(String reason) {
    super(reason);
  }

  public JdbcException(Throwable cause) {
    super(cause);
  }

}
