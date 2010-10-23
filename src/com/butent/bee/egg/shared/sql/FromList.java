package com.butent.bee.egg.shared.sql;

class FromList extends FromSingle {

  public FromList(SqlSelect source, String alias) {
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
