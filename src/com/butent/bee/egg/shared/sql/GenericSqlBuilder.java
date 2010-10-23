package com.butent.bee.egg.shared.sql;

class GenericSqlBuilder extends SqlBuilder {

  @Override
  protected String parseQuotes(String query) {
    return query.replaceAll(SqlUtils.SQL_OPEN_QUOTE + "|"
        + SqlUtils.SQL_CLOSE_QUOTE, "");
  }
}
