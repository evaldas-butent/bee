package com.butent.bee.server.jdbc;

import java.sql.SQLException;

@SuppressWarnings("serial")
public class JdbcException extends SQLException {

  public JdbcException() {
    super();
  }

  public JdbcException(String reason) {
    super(reason);
  }

  public JdbcException(String reason, String sqlState) {
    super(reason, sqlState);
  }

  public JdbcException(String reason, String sqlState, int vendorCode) {
    super(reason, sqlState, vendorCode);
  }

  public JdbcException(String reason, String sqlState, int vendorCode,
      Throwable cause) {
    super(reason, sqlState, vendorCode, cause);
  }

  public JdbcException(String reason, String sqlState, Throwable cause) {
    super(reason, sqlState, cause);
  }

  public JdbcException(String reason, Throwable cause) {
    super(reason, cause);
  }

  public JdbcException(Throwable cause) {
    super(cause);
  }

}
