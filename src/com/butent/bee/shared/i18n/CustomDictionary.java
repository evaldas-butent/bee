package com.butent.bee.shared.i18n;

public interface CustomDictionary {

  String g(String key);

  default String expensesEnteredPerson() {
    return g("expensesEnteredPerson");
  }

  default String trOrdersSumWithProforma() {
    return g("trOrdersSumWithProforma");
  }

  default String trTotalLiabilities() {
    return g("trTotalLiabilities");
  }
}
