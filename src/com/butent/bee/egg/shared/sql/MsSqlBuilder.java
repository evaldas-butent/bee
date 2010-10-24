package com.butent.bee.egg.shared.sql;

class MsSqlBuilder extends SqlBuilder {

  @Override
  protected String sqlQuote(String value) {
    String openQuote = "[";
    String closeQuote = "]";

    return openQuote
        + value.replaceAll(openQuote, "\\\\" + openQuote).replaceAll(
            closeQuote, "\\\\" + closeQuote) + closeQuote;
  }
}
