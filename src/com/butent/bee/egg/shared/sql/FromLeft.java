package com.butent.bee.egg.shared.sql;

class FromLeft extends FromJoin {

  public FromLeft(String source, IsCondition on) {
    super(source, on);
  }

  public FromLeft(String source, String alias, IsCondition on) {
    super(source, alias, on);
  }

  public FromLeft(SqlSelect source, String alias, IsCondition on) {
    super(source, alias, on);
  }

  @Override
  public String getJoinMode() {
    return " LEFT JOIN ";
  }
}
