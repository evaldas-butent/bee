package com.butent.bee.shared.modules.discussions;

import com.butent.bee.shared.i18n.Localized;
import com.butent.bee.shared.ui.HasCaption;

public final class DiscussionsConstants {

  public enum DiscussionStatus implements HasCaption {
    ACTIVE(Localized.getConstants().discussStatusActive()),
    CLOSED(Localized.getConstants().discussStatusClosed());

    private final String caption;

    public static boolean in(int status, DiscussionStatus... statuses) {
      for (DiscussionStatus ts : statuses) {
        if (ts.ordinal() == status) {
          return true;
        }
      }
      return false;
    }

    public boolean is(Integer status) {
      return status != null && ordinal() == status;
    }

    private DiscussionStatus(String caption) {
      this.caption = caption;
    }

    @Override
    public String getCaption() {
      return caption;
    }

  }

  public static final String DISCUSSIONS_MODULE = "Discussions";

  public static final String COL_STATUS = "Status";
  public static final String COL_SUBJECT = "Subject";

  public static final String GRID_DISCUSSIONS = "Discussions";

  public static final String MENU_SERVICE_DISCUSSIONS_LIST = "discuss_list";

  public static final String VIEW_DISCUSSIONS = "Discussions";

  private DiscussionsConstants() {

  }
}
