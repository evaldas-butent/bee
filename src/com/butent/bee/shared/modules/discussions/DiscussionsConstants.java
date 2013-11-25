package com.butent.bee.shared.modules.discussions;

import com.butent.bee.shared.Service;
import com.butent.bee.shared.i18n.Localized;
import com.butent.bee.shared.ui.HasCaption;
import com.butent.bee.shared.utils.EnumUtils;

public final class DiscussionsConstants {

  public enum DiscussionEvent implements HasCaption {
    CREATE(Localized.getConstants().discussEventCreated(), null),
    VISIT(Localized.getConstants().discussEventVisited(), null),
    ACTIVATE(Localized.getConstants().discussEventActivated(), Localized.getConstants()
        .discussActionActivate()),
    COMMENT(Localized.getConstants().discussEventCommented(), Localized.getConstants()
        .discussActionComment()),
    REPLY(Localized.getConstants().discussEventReplied(), Localized.getConstants()
        .discussActionReply()),
    MARK(Localized.getConstants().discussEventMarked(), Localized.getConstants()
        .discussActionMark()),
    MODIFY(Localized.getConstants().discussEventModified(), null),
    DEACTIVATE(Localized.getConstants().discussEventDeactivated(), Localized.getConstants()
        .discussActionDeactivate()),
    CLOSE(Localized.getConstants().discussEventClosed(), Localized.getConstants()
        .discussActionClose());

    private final String caption;
    private final String commandLabel;

    private DiscussionEvent(String caption, String commandLabel) {
      this.caption = caption;
      this.commandLabel = commandLabel;
    }

    @Override
    public String getCaption() {
      return caption;
    }

    public String getCommandLabel() {
      return commandLabel;
    }
  }

  public enum DiscussionStatus implements HasCaption {
    ACTIVE(Localized.getConstants().discussStatusActive()),
    CLOSED(Localized.getConstants().discussStatusClosed()),
    INACTIVE(Localized.getConstants().discussStatusInactive());

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

  public static void register() {
    EnumUtils.register(DiscussionStatus.class);
  }

  public static final String DISCUSSIONS_MODULE = "Discussions";

  public static final String DISCUSSIONS_METHOD = DISCUSSIONS_MODULE + "Method";
  public static final String DISCUSSIONS_PREFIX = "discuss_";
  public static final String DISCUSSIONS_STYLE_PREFIX = "bee-discuss-";
  public static final String STYLE_SHEET = "discuss";

  public static final String COL_ACCESSIBILITY = "Accessibility";
  public static final String COL_CAPTION = "Caption";
  public static final String COL_COMMENT = "Comment";
  public static final String COL_COMMENT_TEXT = "CommentText";
  public static final String COL_DISCUSSION = "Discussion";
  public static final String COL_DESCRIPTION = "Description";
  public static final String COL_FILE = "File";
  public static final String COL_FILE_NAME = "FileName";
  public static final String COL_FILE_SIZE = "FileSize";
  public static final String COL_FILE_TYPE = "FileType";
  public static final String COL_LAST_ACCESS = "LastAccess";
  public static final String COL_MEMBER = "Member";
  public static final String COL_OWNER = "Owner";
  public static final String COL_PUBLISHER = "Publisher";
  public static final String COL_PUBLISHER_FIRST_NAME = "PublisherFirstName";
  public static final String COL_PUBLISHER_LAST_NAME = "PublisherLastName";
  public static final String COL_PUBLISH_TIME = "PublishTime";
  public static final String COL_STAR = "Star";
  public static final String COL_STATUS = "Status";
  public static final String COL_SUBJECT = "Subject";

  public static final String FORM_NEW_DISCUSSION = "NewDiscussion";
  public static final String FORM_DISCUSSION = "Discussion";

  public static final String GRID_DISCUSSIONS = "Discussions";

  public static final String MENU_SERVICE_DISCUSSIONS_LIST = "discuss_list";

  public static final String PROP_COMMENTS = "Comments";
  public static final String PROP_DESCRIPTION = "Description";
  public static final String PROP_FILES = "Files";
  public static final String PROP_LAST_ACCESS = "LastAccess";
  public static final String PROP_LAST_PUBLISH = "LastPublish";
  public static final String PROP_LAST_COMMENT = "LastComment";
  public static final String PROP_MEMBERS = "Members";
  public static final String PROP_STAR = "Star";
  public static final String PROP_USER = "User";

  public static final String PROP_COMPANIES = "Companies";
  public static final String PROP_PERSONS = "Persons";
  public static final String PROP_APPOINTMENTS = "Appointments";
  public static final String PROP_TASKS = "Tasks";
  public static final String PROP_DOCUMENTS = "Documents";

  public static final String SVC_GET_DISCUSSION_DATA = "get_discuss_data";

  public static final String TBL_DISCUSSIONS_COMMENTS = "DiscussionsComments";
  public static final String TBL_DISCUSSIONS_USERS = "DiscussionsUsers";

  public static final long VALUE_MEMBER = 1;

  public static final String VAR_DISCUSSION_COMMENT = Service.RPC_VAR_PREFIX + "discuss_comment";
  public static final String VAR_DISCUSSION_DATA = Service.RPC_VAR_PREFIX + "discuss_data";
  public static final String VAR_DISCUSSION_ID = Service.RPC_VAR_PREFIX + "discuss_id";
  public static final String VAR_DISCUSSION_RELATIONS = Service.RPC_VAR_PREFIX
      + "discuss_relations";
  public static final String VAR_DISCUSSION_USERS = Service.RPC_VAR_PREFIX + "discuss_users";

  public static final String VIEW_DISCUSSIONS = "Discussions";
  public static final String VIEW_DISCUSSIONS_COMMENTS = "DiscussionsComments";
  public static final String VIEW_DISCUSSIONS_FILES = "DiscussionsFiles";
  public static final String VIEW_DISCUSSIONS_USERS = "DiscussionsUsers";

  private DiscussionsConstants() {

  }
}
