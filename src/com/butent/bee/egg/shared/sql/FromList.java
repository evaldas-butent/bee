package com.butent.bee.egg.shared.sql;

public class FromList extends FromSingle {

  public FromList(QueryBuilder source, String alias) {
    super(source, alias);
  }

  public FromList(String source) {
    super(source);
  }

  public FromList(String source, String alias) {
    super(source, alias);
  }

  @Override
  public String getJoinMode() {
    return ", ";
  }
}
