package com.butent.bee.egg.shared.sql;

class FromLeft extends FromJoin {

  public FromLeft(SqlSelect source, String alias, Condition on) {
    super(source, alias, on);
  }

  public FromLeft(String source, Condition on) {
    super(source, on);
  }

  public FromLeft(String source, String alias, Condition on) {
    super(source, alias, on);
  }

  @Override
  public String getJoinMode() {
    return " LEFT JOIN ";
  }
}
