package com.butent.bee.shared.modules.projects;

import com.butent.bee.shared.i18n.Localized;
import com.butent.bee.shared.ui.HasCaption;

/**
 * Priorities of project.
 */
public enum ProjectPriority implements HasCaption {
  /**
   * Low priority type of project.
   */
  LOW(Localized.dictionary().lowPriority()),

  /**
   * Medium priority type of project.
   */
  MEDIUM(Localized.dictionary().mediumPriority()),

  /**
   * High priority type of project.
   */
  HIGH(Localized.dictionary().highPriority());

  private String caption;

  ProjectPriority(String caption) {
    this.caption = caption;
  }

  @Override
  public String getCaption() {
    return caption;
  }

}
