package com.butent.bee.server.sql;

import com.butent.bee.shared.BeeConst.SqlEngine;

/**
 * Invokes SQL statement builders for MySQL, Microsoft SQL Server, Oracle, PostgreSQL servers, sets
 * default SQL builder for a particular instance of application.
 */

public class SqlBuilderFactory {

  private static String defaultDsn;
  private static SqlBuilder defaultBuilder = getBuilder(SqlEngine.GENERIC);

  /**
   * @return the default builder.
   */
  public static SqlBuilder getBuilder() {
    return defaultBuilder;
  }

  /**
   * Creates and returns a builder defined by the specified engine {@code engine}.
   * 
   * @param engine the engine for a builder
   * @return a new builder
   */
  public static SqlBuilder getBuilder(SqlEngine engine) {
    SqlBuilder builder = null;

    if (engine != null) {
      switch (engine) {
        case MYSQL:
          builder = new MySqlBuilder();
          break;
        case MSSQL:
          builder = new MsSqlBuilder();
          break;
        case ORACLE:
          builder = new OracleSqlBuilder();
          break;
        case POSTGRESQL:
          builder = new PostgreSqlBuilder();
          break;
        case GENERIC:
          builder = new GenericSqlBuilder();
          break;
      }
    }
    return builder;
  }

  /**
   * @return the default data source name.
   */
  public static String getDsn() {
    return defaultDsn;
  }

  /**
   * Sets {@code engine} as a default engine.
   * 
   * @param engine SQL engine to set.
   * @return {@code true}, if engine is valid and builder was set successfully
   */
  public static boolean setDefaultBuilder(SqlEngine engine) {
    return setDefaultBuilder(engine, null);
  }

  /**
   * Sets {@code engine} as a default engine.
   * 
   * @param engine SQL engine to set.
   * @param dsn data source name, to which engine is bound.
   * @return {@code true}, if engine is valid and builder was set successfully
   */
  public static synchronized boolean setDefaultBuilder(SqlEngine engine, String dsn) {
    SqlBuilder builder = getBuilder(engine);
    boolean ok = (builder != null);

    if (ok) {
      defaultDsn = dsn;
      defaultBuilder = builder;
    }
    return ok;
  }
}
