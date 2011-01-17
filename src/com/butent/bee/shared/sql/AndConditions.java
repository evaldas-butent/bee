package com.butent.bee.shared.sql;

class AndConditions extends Conditions {

  @Override
  protected String joinMode() {
    return " AND ";
  }
}
