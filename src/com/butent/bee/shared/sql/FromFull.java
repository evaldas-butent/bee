package com.butent.bee.shared.sql;

class FromFull extends FromJoin {

  public FromFull(String source, IsCondition on) {
    super(source, on);
  }

  public FromFull(String source, String alias, IsCondition on) {
    super(source, alias, on);
  }

  public FromFull(SqlSelect source, String alias, IsCondition on) {
    super(source, alias, on);
  }

  @Override
  public String getJoinMode() {
    return " FULL JOIN ";
  }
}
