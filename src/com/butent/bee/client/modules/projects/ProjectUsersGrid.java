package com.butent.bee.client.modules.projects;

import com.butent.bee.client.view.grid.interceptor.AbstractGridInterceptor;
import com.butent.bee.client.view.grid.interceptor.GridInterceptor;

class ProjectUsersGrid extends AbstractGridInterceptor {

  @Override
  public GridInterceptor getInstance() {
    return new ProjectUsersGrid();
  }


}
