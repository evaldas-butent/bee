package com.butent.bee.shared.data;

public abstract class RowToDouble implements RowFunction<Double> {

  public static RowToDouble at(final int index) {
    return new RowToDouble() {
      @Override
      public Double apply(IsRow input) {
        return (input == null) ? null : input.getDouble(index);
      }
    };
  }
}
