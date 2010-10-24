package com.butent.bee.egg.shared.sql;

class MySqlBuilder extends SqlBuilder {

  @Override
  protected String sqlQuote(String value) {
    String quote = "`";

    return quote + value.replaceAll(quote, "\\\\" + quote) + quote;
  }
}
