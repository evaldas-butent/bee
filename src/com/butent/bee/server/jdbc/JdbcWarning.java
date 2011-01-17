package com.butent.bee.server.jdbc;

import java.sql.SQLWarning;

@SuppressWarnings("serial")
public class JdbcWarning extends SQLWarning {

  public JdbcWarning() {
    super();
  }

  public JdbcWarning(String reason) {
    super(reason);
  }

  public JdbcWarning(String reason, String sqlState) {
    super(reason, sqlState);
  }

  public JdbcWarning(String reason, String sqlState, int vendorCode) {
    super(reason, sqlState, vendorCode);
  }

  public JdbcWarning(String reason, String sqlState, int vendorCode,
      Throwable cause) {
    super(reason, sqlState, vendorCode, cause);
  }

  public JdbcWarning(String reason, String sqlState, Throwable cause) {
    super(reason, sqlState, cause);
  }

  public JdbcWarning(String reason, Throwable cause) {
    super(reason, cause);
  }

  public JdbcWarning(Throwable cause) {
    super(cause);
  }

}
