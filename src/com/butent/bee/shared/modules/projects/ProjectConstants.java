package com.butent.bee.shared.modules.projects;

import com.butent.bee.shared.utils.EnumUtils;

/**
 * Constants of Projects module.
 */
public final class ProjectConstants {

  /**
   * Register module Enumerations.
   */
  public static void register() {
    EnumUtils.register(ProjectPriority.class);
    EnumUtils.register(ProjectStatus.class);
    EnumUtils.register(ProjectUserType.class);
  }

  private ProjectConstants() {
  }
}
