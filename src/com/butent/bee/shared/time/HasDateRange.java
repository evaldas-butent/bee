package com.butent.bee.shared.time;

import com.google.common.collect.Range;

public interface HasDateRange {
  Range<JustDate> getRange();
}
