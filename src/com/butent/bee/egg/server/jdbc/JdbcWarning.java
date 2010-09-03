package com.butent.bee.egg.server.jdbc;

import java.sql.SQLWarning;

@SuppressWarnings("serial")
public class JdbcWarning extends SQLWarning {

  public JdbcWarning() {
    super();
  }

  public JdbcWarning(String reason, String SQLState, int vendorCode,
      Throwable cause) {
    super(reason, SQLState, vendorCode, cause);
  }

  public JdbcWarning(String reason, String SQLState, int vendorCode) {
    super(reason, SQLState, vendorCode);
  }

  public JdbcWarning(String reason, String SQLState, Throwable cause) {
    super(reason, SQLState, cause);
  }

  public JdbcWarning(String reason, String SQLState) {
    super(reason, SQLState);
  }

  public JdbcWarning(String reason, Throwable cause) {
    super(reason, cause);
  }

  public JdbcWarning(String reason) {
    super(reason);
  }

  public JdbcWarning(Throwable cause) {
    super(cause);
  }

}
