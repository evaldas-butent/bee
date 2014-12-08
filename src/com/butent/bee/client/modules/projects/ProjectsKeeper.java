package com.butent.bee.client.modules.projects;

import static com.butent.bee.shared.modules.projects.ProjectConstants.*;

import com.butent.bee.client.grid.GridFactory;

/**
 * Client-side projects module handler.
 */
public final class ProjectsKeeper {

  /**
   * Register projects client-side module handler.
   */
  public static void register() {
    /* Register grid handlers */
    GridFactory.registerGridInterceptor(GRID_PROJECTS, new ProjectsGrid());
  }

  private ProjectsKeeper() {

  }
}
