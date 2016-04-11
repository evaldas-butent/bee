package com.butent.bee.shared.data;

public abstract class RowToLong implements RowFunction<Long> {

  public static RowToLong at(final int index) {
    return new RowToLong() {
      @Override
      public Long apply(IsRow input) {
        return (input == null) ? null : input.getLong(index);
      }
    };
  }
}
