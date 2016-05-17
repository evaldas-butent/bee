package com.butent.bee.shared.utils;

import com.google.common.collect.BoundType;
import com.google.common.collect.Range;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * A simplified implementation of Guava RangeSet.
 */
public class IntRangeSet {

  private final Set<Range<Integer>> ranges = new HashSet<>();

  public IntRangeSet() {
  }

  public boolean add(Range<Integer> range) {
    Range<Integer> nr = normalize(range);
    if (nr == null) {
      return false;
    }

    if (ranges.isEmpty()) {
      ranges.add(nr);
      return true;
    }

    for (Range<Integer> r : ranges) {
      if (r.encloses(nr)) {
        return false;
      }
    }

    List<Range<Integer>> list = asList();
    ranges.clear();

    for (Range<Integer> r : list) {
      if (isConnected(r, nr)) {
        nr = span(r, nr);
      } else {
        ranges.add(r);
      }
    }

    ranges.add(nr);

    return true;
  }

  public boolean addClosed(int lower, int upper) {
    if (lower <= upper) {
      return add(Range.closed(lower, upper));
    } else {
      return false;
    }
  }

  public boolean addClosedOpen(int lower, int upper) {
    return addClosed(lower, upper - 1);
  }

  public List<Range<Integer>> asList() {
    List<Range<Integer>> result = new ArrayList<>();

    if (!ranges.isEmpty()) {
      result.addAll(ranges);

      if (result.size() > 1) {
        Collections.sort(result,
            (r1, r2) -> Integer.compare(r1.lowerEndpoint(), r2.lowerEndpoint()));
      }
    }

    return result;
  }

  public void clear() {
    ranges.clear();
  }

  public IntRangeSet complement(int min, int max) {
    IntRangeSet result = new IntRangeSet();
    if (min > max) {
      return result;
    }

    result.addClosed(min, max);

    for (Range<Integer> r : ranges) {
      result.remove(r);
    }

    return result;
  }

  @Override
  public boolean equals(Object obj) {
    if (obj instanceof IntRangeSet) {
      return ranges.equals(((IntRangeSet) obj).ranges);
    } else {
      return false;
    }
  }

  @Override
  public int hashCode() {
    return ranges.hashCode();
  }

  public boolean isEmpty() {
    return ranges.isEmpty();
  }

  public boolean remove(Range<Integer> range) {
    Range<Integer> nr = normalize(range);
    if (nr == null) {
      return false;
    }

    if (ranges.isEmpty()) {
      return false;
    }

    Set<Range<Integer>> in = new HashSet<>(ranges);
    ranges.clear();

    boolean changed = false;

    for (Range<Integer> r : in) {
      if (nr.encloses(r)) {
        changed = true;

      } else {
        Range<Integer> intersection = intersection(r, nr);

        if (intersection == null) {
          ranges.add(r);

        } else {
          if (intersection.lowerEndpoint() > r.lowerEndpoint()) {
            ranges.add(Range.closed(r.lowerEndpoint(), intersection.lowerEndpoint() - 1));
          }
          if (intersection.upperEndpoint() < r.upperEndpoint()) {
            ranges.add(Range.closed(intersection.upperEndpoint() + 1, r.upperEndpoint()));
          }

          changed = true;
        }
      }
    }

    return changed;
  }

  public boolean removeClosed(int lower, int upper) {
    if (lower <= upper) {
      return remove(Range.closed(lower, upper));
    } else {
      return false;
    }
  }

  public boolean removeClosedOpen(int lower, int upper) {
    return removeClosed(lower, upper - 1);
  }

  public int size() {
    return ranges.size();
  }

  @Override
  public String toString() {
    return ranges.toString();
  }

  private static Range<Integer> intersection(Range<Integer> r1, Range<Integer> r2) {
    if (BeeUtils.intersects(r1, r2)) {
      return normalize(r1.intersection(r2));
    } else {
      return null;
    }
  }

  private static boolean isConnected(Range<Integer> r1, Range<Integer> r2) {
    return r1.lowerEndpoint() <= r2.upperEndpoint() + 1
        && r2.lowerEndpoint() <= r1.upperEndpoint() + 1;
  }

  private static Range<Integer> normalize(Range<Integer> range) {
    if (range == null || range.isEmpty()) {
      return null;

    } else if (range.hasLowerBound() && range.lowerBoundType() == BoundType.CLOSED
        && range.hasUpperBound() && range.upperBoundType() == BoundType.CLOSED) {

      return range;

    } else {
      int lower;
      int upper;

      if (range.hasLowerBound()) {
        lower = range.lowerEndpoint();
        if (range.lowerBoundType() == BoundType.OPEN) {
          lower++;
        }
      } else {
        lower = Integer.MIN_VALUE;
      }

      if (range.hasUpperBound()) {
        upper = range.upperEndpoint();
        if (range.upperBoundType() == BoundType.OPEN) {
          upper--;
        }
      } else {
        upper = Integer.MAX_VALUE;
      }

      if (lower <= upper) {
        return Range.closed(lower, upper);
      } else {
        return null;
      }
    }
  }

  private static Range<Integer> span(Range<Integer> r1, Range<Integer> r2) {
    return Range.closed(Math.min(r1.lowerEndpoint(), r2.lowerEndpoint()),
        Math.max(r1.upperEndpoint(), r2.upperEndpoint()));
  }
}
