package com.butent.bee.client.ui;

import com.butent.bee.shared.data.IsRow;

public interface HasParent {
  void refresh(IsRow parentRow, Boolean parentEnabled);
}
