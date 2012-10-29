package com.butent.bee.client.render;

import com.butent.bee.shared.data.IsRow;

public interface HandlesRendering extends HasCellRenderer {
  void render(IsRow row);
}
