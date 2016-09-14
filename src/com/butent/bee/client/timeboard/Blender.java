package com.butent.bee.client.timeboard;

import com.butent.bee.shared.time.HasDateRange;

@FunctionalInterface
public interface Blender {
  boolean willItBlend(HasDateRange x, HasDateRange y);
}
