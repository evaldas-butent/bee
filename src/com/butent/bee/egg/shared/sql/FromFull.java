package com.butent.bee.egg.shared.sql;

class FromFull extends FromJoin {

  public FromFull(SqlSelect source, String alias, Condition on) {
    super(source, alias, on);
  }

  public FromFull(String source, Condition on) {
    super(source, on);
  }

  public FromFull(String source, String alias, Condition on) {
    super(source, alias, on);
  }

  @Override
  public String getJoinMode() {
    return " FULL JOIN ";
  }
}
