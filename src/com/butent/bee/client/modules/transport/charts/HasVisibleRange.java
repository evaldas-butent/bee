package com.butent.bee.client.modules.transport.charts;

import com.google.common.collect.Range;

import com.butent.bee.shared.time.JustDate;

interface HasVisibleRange {
  
  Range<JustDate> getMaxRange();
  
  int getMaxSize();

  Range<JustDate> getVisibleRange();
  
  boolean setVisibleRange(JustDate start, JustDate end);
}
