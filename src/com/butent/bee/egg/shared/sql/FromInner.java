package com.butent.bee.egg.shared.sql;

class FromInner extends FromJoin {

  public FromInner(SqlSelect source, String alias, Condition on) {
    super(source, alias, on);
  }

  public FromInner(String source, Condition on) {
    super(source, on);
  }

  public FromInner(String source, String alias, Condition on) {
    super(source, alias, on);
  }

  @Override
  public String getJoinMode() {
    return " INNER JOIN ";
  }
}
