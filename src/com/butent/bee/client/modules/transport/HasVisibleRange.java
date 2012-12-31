package com.butent.bee.client.modules.transport;

import com.google.common.collect.Range;

import com.butent.bee.shared.time.JustDate;

interface HasVisibleRange {
  
  Range<JustDate> getMaxRange();

  Range<JustDate> getVisibleRange();
  
  void setVisibleEnd(JustDate end);

  void setVisibleRange(JustDate start, JustDate end);

  void setVisibleStart(JustDate start);
}
