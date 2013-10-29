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

  public static final String COL_DISCUSSION = "Discussion";
  public static final String COL_LAST_ACCESS = "LastAccess";
  public static final String COL_PUBLISHER = "Publisher";
  public static final String COL_PUBLISH_TIME = "PublishTime";
  public static final String COL_STAR = "Star";
  public static final String COL_STATUS = "Status";
  public static final String COL_SUBJECT = "Subject";

  public static final String FORM_NEW_DISCUSSION = "NewDiscussion";

  public static final String GRID_DISCUSSIONS = "Discussions";

  public static final String MENU_SERVICE_DISCUSSIONS_LIST = "discuss_list";

  public static final String PROP_LAST_ACCESS = "LastAccess";
  public static final String PROP_LAST_PUBLISH = "LastPublish";
  public static final String PROP_STAR = "Star";
  public static final String PROP_USER = "User";

  public static final String TBL_DISCUSSIONS_COMMENTS = "DiscussionsComments";
  public static final String TBL_DISCUSSIONS_USERS = "DiscussionsUsers";

  public static final String VIEW_DISCUSSIONS = "Discussions";
  public static final String VIEW_DISCUSSIONS_USERS = "DiscussionsUsers";

  private DiscussionsConstants() {

  }
}
