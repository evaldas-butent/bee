package com.butent.bee.shared.modules.projects;

import static com.butent.bee.shared.modules.projects.ProjectConstants.*;
import com.butent.bee.shared.i18n.Dictionary;
import com.butent.bee.shared.ui.HasLocalizedCaption;

/**
 * Statuses of project.
 */
public enum ProjectStatus implements HasLocalizedCaption {
  ACTIVE {
    @Override
    public String getCaption(Dictionary constants) {
      return constants.prjStatusActive();
    }

    @Override
    public String getStyleName(boolean differentBorder) {
      if (differentBorder) {
        return PROJECT_STATUS_STYLE_ACTIVE + PROJECT_STATUS_STYLE_WITH_BORDER;
      } else {
        return PROJECT_STATUS_STYLE_ACTIVE;
      }
    }
  },

  SCHEDULED {
    @Override
    public String getCaption(Dictionary constants) {
      return constants.prjStatusScheduled();
    }

    @Override
    public String getStyleName(boolean differentBorder) {
      return null;
    }
  },

  SUSPENDED {
    @Override
    public String getCaption(Dictionary constants) {
      return constants.prjStatusSuspended();
    }

    @Override
    public String getStyleName(boolean differentBorder) {
      if (differentBorder) {
        return PROJECT_STATUS_STYLE_SUSPENDED + PROJECT_STATUS_STYLE_WITH_BORDER;
      } else {
        return PROJECT_STATUS_STYLE_SUSPENDED;
      }
    }
  },

  APPROVED {
    @Override
    public String getCaption(Dictionary constants) {
      return constants.prjStatusApproved();
    }

    @Override
    public String getStyleName(boolean differentBorder) {
      return null;
    }
  };

  /**
   * Method used for rendering css elements according to project status.
   * @param differentBorder - indicates then different elements style is needed.
   * @return css class name.
   */
  public abstract String getStyleName(boolean differentBorder);
}
