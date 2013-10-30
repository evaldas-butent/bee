package com.butent.bee.shared.data.event;

import com.butent.bee.shared.data.HasViewName;

/**
 * Determines that data events are related to operations with views.
 */

public interface DataEvent extends HasViewName {
  boolean hasView(String view);
}
