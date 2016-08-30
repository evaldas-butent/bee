package com.butent.bee.client.modules.projects;

import static com.butent.bee.shared.modules.tasks.TaskConstants.*;

import com.butent.bee.client.render.AbstractRowModeRenderer;
import com.butent.bee.shared.data.IsRow;

class ModeRenderer extends AbstractRowModeRenderer {

  @Override
  public boolean hasUserProperty(IsRow row, Long userId) {
    return true;
  }

  @Override
  public Long getLastAccess(IsRow row, Long userId) {
    return row.getPropertyLong(PROP_LAST_ACCESS, userId);
  }

  @Override
  public Long getLastUpdate(IsRow row, Long userId) {
    return row.getPropertyLong(PROP_LAST_PUBLISH);
  }

  ModeRenderer() {
    super();
  }

}
