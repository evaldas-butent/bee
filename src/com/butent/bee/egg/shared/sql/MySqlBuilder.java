package com.butent.bee.egg.shared.sql;

import com.butent.bee.egg.shared.sql.BeeConstants.Keywords;

class MySqlBuilder extends SqlBuilder {

  @Override
  protected String sqlKeyword(Keywords option, Object... params) {
    switch (option) {
      case DROP_FOREIGNKEY:
        StringBuilder drop = new StringBuilder("ALTER TABLE ")
          .append(params[0])
          .append(" DROP FOREIGN KEY ")
          .append(params[1]);
        return drop.toString();
      default:
        return super.sqlKeyword(option, params);
    }
  }

  @Override
  protected String sqlQuote(String value) {
    return "`" + value + "`";
  }

  @Override
  String getCreate(SqlCreate sc, boolean paramMode) {
    String sql = super.getCreate(sc, paramMode);

    if (!sql.contains(" AS SELECT ")) {
      sql += " ENGINE=InnoDB";
    }
    return sql;
  }
}
