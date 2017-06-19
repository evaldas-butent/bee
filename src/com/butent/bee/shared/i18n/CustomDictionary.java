package com.butent.bee.shared.i18n;

public interface CustomDictionary {

  String g(String key);

  default String expensesEnteredPerson() {
    return g("expensesEnteredPerson");
  }
}
