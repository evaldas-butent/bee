package com.butent.bee.egg.shared.sql;

public class FromRight extends FromJoin {

  public FromRight(QueryBuilder source, String alias, Condition on) {
    super(source, alias, on);
  }

  public FromRight(String source, Condition on) {
    super(source, on);
  }

  public FromRight(String source, String alias, Condition on) {
    super(source, alias, on);
  }

  @Override
  public String getJoinMode() {
    return " RIGHT JOIN ";
  }
}
