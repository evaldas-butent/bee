package com.butent.bee.egg.shared.sql;

class AndConditions extends Conditions {

  @Override
  protected String joinMode() {
    return " AND ";
  }
}
