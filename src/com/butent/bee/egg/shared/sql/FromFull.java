package com.butent.bee.egg.shared.sql;

public class FromFull extends FromJoin {

  public FromFull(QueryBuilder source, String alias, Condition on) {
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
