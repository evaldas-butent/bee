package com.butent.bee.shared.utils;

import com.google.common.base.Predicate;

public enum StringPredicate implements Predicate<String> {
  
  IS_EMPTY {
    public boolean apply(String s) {
      return s == null || s.isEmpty();
    }
  },
  
  NOT_EMPTY {
    public boolean apply(String s) {
      return s != null && !s.isEmpty();
    }
  },
  
  IS_NULL {
    public boolean apply(String s) {
      return s == null;
    }
  },

  NOT_NULL {
    public boolean apply(String s) {
      return s != null;
    }
  }
}