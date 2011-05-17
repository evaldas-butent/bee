package com.butent.bee.shared;

import java.util.HashMap;

/**
 * Enables using arrays to store ranges with minimum and maximum values.
 */

@SuppressWarnings("serial")
public class RangeMap<V> extends HashMap<RangeMap<V>.Range, V> {

  public class Range extends Pair<Double, Double> {

    public Range(Double min, Double max) {
      super(min, max);
    }

    public boolean contains(double value) {
      return (getMin() == null || getMin() <= value) && (getMax() == null || getMax() > value);
    }

    public boolean contains(Range range) {
      if (range == null) {
        return false;
      }
      if (equals(range)) {
        return true;
      }

      if (range.getMin() == null) {
        if (range.getMax() == null) {
          return getMin() == null && getMax() == null;
        }
        return getMin() == null && getMax() >= range.getMax();
      }
      if (range.getMax() == null) {
        return getMax() == null && getMin() <= range.getMin();
      }

      return (getMin() == null || getMin() <= range.getMin())
          && (getMax() == null || getMax() >= range.getMax());
    }

    public Double getMax() {
      return getB();
    }

    public Double getMin() {
      return getA();
    }
  }

  public static <V> RangeMap<V> create() {
    return new RangeMap<V>();
  }

  public static <V> RangeMap<V> create(Double min, Double max, V value) {
    RangeMap<V> rangeMap = new RangeMap<V>();
    rangeMap.put(min, max, value);
    return rangeMap;
  }

  public static <V> RangeMap<V> create(Double min1, Double max1, V value1,
      Double min2, Double max2, V value2) {
    RangeMap<V> rangeMap = new RangeMap<V>();
    rangeMap.put(min1, max1, value1);
    rangeMap.put(min2, max2, value2);
    return rangeMap;
  }

  public static <V> RangeMap<V> create(Double min1, Double max1, V value1,
      Double min2, Double max2, V value2, Double min3, Double max3, V value3) {
    RangeMap<V> rangeMap = new RangeMap<V>();
    rangeMap.put(min1, max1, value1);
    rangeMap.put(min2, max2, value2);
    rangeMap.put(min3, max3, value3);
    return rangeMap;
  }

  private RangeMap() {
    super();
  }

  public V get(double key) {
    Range bestRange = null;

    for (Range range : keySet()) {
      if (range != null && range.contains(key)
          && (bestRange == null || bestRange.contains(range))) {
        bestRange = range;
      }
    }

    if (bestRange == null) {
      return null;
    }
    return super.get(bestRange);
  }

  public V put(Double min, Double max, V value) {
    return super.put(new Range(min, max), value);
  }
}
