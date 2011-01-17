package com.butent.bee.server.datasource.util;

public class SqlDatabaseDescription {
  private String password;
  private String tableName;
  private String url;
  private String user;

  public SqlDatabaseDescription(String url, String user, String password, String tableName) {
    this.url = url;
    this.user = user;
    this.password = password;
    this.tableName = tableName;
  }

  public String getPassword() {
    return password;
  }

  public String getTableName() {
    return tableName;
  }

  public String getUrl() {
    return url;
  }

  public String getUser() {
    return user;
  } 

  public void setPassword(String password) {
    this.password = password;
  }
}
