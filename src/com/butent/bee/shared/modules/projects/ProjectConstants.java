package com.butent.bee.shared.modules.projects;

import com.butent.bee.shared.utils.EnumUtils;

/**
 * Constants of Projects module.
 */
public final class ProjectConstants {

  public static final String COL_PROJECT = "Project";
  public static final String COL_PROJECT_STAGE = "ProjectStage";
  public static final String COL_PROJECT_STATUS = "Status";
  public static final String COL_PROJECT_OWNER = "Owner";

  public static final String COL_USER_TYPE = "UserType";

  public static final String FORM_PROJECT = "Project";

  public static final String GRID_ALL_PROJECTS = "AllProjects";
  public static final String GRID_PROJECTS = "Projects";
  public static final String GRID_PROJECT_USERS = "ProjectUsers";

  public static final String VIEW_PROJECTS = "Projects";
  public static final String VIEW_PROJECT_USERS = "ProjectUsers";
  public static final String VIEW_PROJECT_STAGES = "ProjectStages";

  /**
   * Register module Enumerations.
   */
  public static void register() {
    EnumUtils.register(ProjectPriority.class);
    EnumUtils.register(ProjectStatus.class);
  }

  private ProjectConstants() {
  }
}
