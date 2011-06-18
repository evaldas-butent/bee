package com.butent.bee.client.view.search;

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
