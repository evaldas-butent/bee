package com.butent.bee.shared.modules.projects;

import com.butent.bee.shared.i18n.LocalizableConstants;
import com.butent.bee.shared.i18n.Localized;
import com.butent.bee.shared.ui.HasLocalizedCaption;

/**
 * Statuses of project.
 */
public enum ProjectStatus implements HasLocalizedCaption {
  ACTIVE {
    @Override
    public String getCaption(LocalizableConstants constants) {
      return constants.prjStatusActive();
    }
  },

  SCHEDULED {
    @Override
    public String getCaption(LocalizableConstants constants) {
      return constants.prjStatusScheduled();
    }
  },

  SUSPENDED {
    @Override
    public String getCaption(LocalizableConstants constants) {
      return constants.prjStatusSuspended();
    }
  },

  COMPLETED {
    @Override
    public String getCaption(LocalizableConstants constants) {
      return constants.prjStatusCompleted();
    }
  },

  APPROVED {
    @Override
    public String getCaption(LocalizableConstants constants) {
      return constants.prjStatusApproved();
    }
  },

  CANCELED {
    @Override
    public String getCaption(LocalizableConstants constants) {
      return constants.prjStatusCanceled();
    }
  };

  @Override
  public String getCaption() {
    return getCaption(Localized.getConstants());
  }
}
