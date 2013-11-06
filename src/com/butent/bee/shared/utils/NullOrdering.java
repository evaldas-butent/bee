package com.butent.bee.shared.utils;

public enum NullOrdering {
  NULLS_FIRST, NULLS_LAST;
  
  public static final NullOrdering DEFAULT = NULLS_LAST;
}
