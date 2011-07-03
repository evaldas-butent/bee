package com.butent.bee.client.view.search;

/**
 * Contains a list of possible search types (starts, contains, filter).
 */

public enum SearchType {
  STARTS('='), CONTAINS('\u2282'), FILTER('?');

  private final char symbol;

  private SearchType(char symbol) {
    this.symbol = symbol;
  }

  public char getSymbol() {
    return symbol;
  }
}
