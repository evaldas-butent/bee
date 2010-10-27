package com.butent.bee.egg.shared.sql;

class FromInner extends FromJoin {

  public FromInner(String source, IsCondition on) {
    super(source, on);
  }

  public FromInner(String source, String alias, IsCondition on) {
    super(source, alias, on);
  }

  public FromInner(SqlSelect source, String alias, IsCondition on) {
    super(source, alias, on);
  }

  @Override
  public String getJoinMode() {
    return " INNER JOIN ";
  }
}
