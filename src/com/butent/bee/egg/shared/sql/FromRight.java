package com.butent.bee.egg.shared.sql;

class FromRight extends FromJoin {

  public FromRight(String source, IsCondition on) {
    super(source, on);
  }

  public FromRight(String source, String alias, IsCondition on) {
    super(source, alias, on);
  }

  public FromRight(SqlSelect source, String alias, IsCondition on) {
    super(source, alias, on);
  }

  @Override
  public String getJoinMode() {
    return " RIGHT JOIN ";
  }
}
