package com.butent.bee.shared.data;

import com.butent.bee.shared.utils.BeeUtils;

import java.util.function.Predicate;

public abstract class RowPredicate implements Predicate<IsRow> {

  public static RowPredicate isNull(final int index) {
    return new RowPredicate() {
      @Override
      public boolean test(IsRow input) {
        return input == null || input.isNull(index);
      }
    };
  }

  public static RowPredicate isTrue(final int index) {
    return new RowPredicate() {
      @Override
      public boolean test(IsRow input) {
        return input != null && BeeUtils.isTrue(input.getBoolean(index));
      }
    };
  }

  public static RowPredicate notNull(final int index) {
    return new RowPredicate() {
      @Override
      public boolean test(IsRow input) {
        return input != null && !input.isNull(index);
      }
    };
  }
}
