package com.butent.bee.shared.data;

@FunctionalInterface
public interface RowToDouble extends RowFunction<Double> {

  static RowToDouble at(int index) {
    return input -> (input == null) ? null : input.getDouble(index);
  }

  default RowToDouble minus(RowToDouble other) {
    return input -> {
      Double v1 = apply(input);
      Double v2 = (other == null) ? null : other.apply(input);

      if (v1 == null) {
        return v2;
      } else if (v2 == null) {
        return v1;
      } else {
        return v1 - v2;
      }
    };
  }

  default RowToDouble plus(RowToDouble other) {
    return input -> {
      Double v1 = apply(input);
      Double v2 = (other == null) ? null : other.apply(input);

      if (v1 == null) {
        return v2;
      } else if (v2 == null) {
        return v1;
      } else {
        return v1 + v2;
      }
    };
  }

  default RowToDouble times(RowToDouble other) {
    return input -> {
      Double v1 = apply(input);
      Double v2 = (other == null) ? null : other.apply(input);

      return (v1 == null || v2 == null) ? null : (v1 * v2);
    };
  }
}
