package com.butent.bee.egg.shared.sql;

class MsSqlBuilder extends SqlBuilder {

  @Override
  protected String sqlQuote(String value) {
    String openQuote = "[";
    String closeQuote = "]";

    return openQuote
        + value.replaceAll(escapeRegex(openQuote), "\\" + openQuote)
          .replaceAll(escapeRegex(closeQuote), "\\" + closeQuote)
        + closeQuote;
  }

  private String escapeRegex(String s) {
    return "\\" + s;
  }
}
