package com.butent.bee.shared.utils;

import java.util.function.Predicate;

public enum StringPredicate implements Predicate<String> {

  IS_EMPTY {
    @Override
    public boolean test(String s) {
      return s == null || s.isEmpty();
    }
  },

  NOT_EMPTY {
    @Override
    public boolean test(String s) {
      return s != null && !s.isEmpty();
    }
  },

  IS_NULL {
    @Override
    public boolean test(String s) {
      return s == null;
    }
  },

  NOT_NULL {
    @Override
    public boolean test(String s) {
      return s != null;
    }
  }
}