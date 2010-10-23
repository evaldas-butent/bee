package com.butent.bee.egg.shared.sql;

class MsSqlBuilder extends SqlBuilder {

  @Override
  protected String parseQuotes(String query) {
    String s = query.replaceAll(SqlUtils.SQL_OPEN_QUOTE, "[").replaceAll(
        SqlUtils.SQL_CLOSE_QUOTE, "]");
    return s;
  }
}
