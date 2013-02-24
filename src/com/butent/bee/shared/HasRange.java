package com.butent.bee.shared;

import com.google.common.collect.Range;

public interface HasRange<C extends Comparable<C>> {
  Range<C> getRange();
}
