package com.butent.bee.egg.shared.sql;

public class AndConditions extends Conditions {

  @Override
  protected String joinMode() {
    return " AND ";
  }
}
