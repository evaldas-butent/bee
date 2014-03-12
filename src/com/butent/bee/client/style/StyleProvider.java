package com.butent.bee.client.style;

import com.butent.bee.shared.data.IsRow;

public interface StyleProvider {
  StyleDescriptor getStyleDescriptor(IsRow row);
}
