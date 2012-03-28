package com.butent.bee.client.ui;

import com.butent.bee.shared.data.IsRow;

public interface FosterChild {
  boolean hasFosterParent(String fosterParent);
  
  void takeCare(String fosterParent, IsRow parentRow, Boolean parentEnabled);
}
