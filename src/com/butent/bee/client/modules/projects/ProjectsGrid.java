package com.butent.bee.client.modules.projects;

import com.butent.bee.client.view.grid.interceptor.GridInterceptor;

class ProjectsGrid extends AllProjectsGrid {

  @Override
  public GridInterceptor getInstance() {
    return new ProjectsGrid();
  }

}
