package com.butent.bee.shared.modules.crm;

import com.butent.bee.shared.Service;
import com.butent.bee.shared.ui.HasCaption;
import com.butent.bee.shared.utils.BeeUtils;

public class CrmConstants {

  public enum Priority implements HasCaption {
    LOW("Žemas"), MEDIUM("Vidutinis"), HIGH("Aukštas");

    private final String caption;

    private Priority(String caption) {
      this.caption = caption;
    }

    @Override
    public String getCaption() {
      return caption;
    }
  }

  public enum ProjectEvent implements HasCaption {
    CREATED, ACTIVATED, SUSPENDED, COMPLETED, CANCELED,
    EXTENDED, RENEWED, COMMENTED, VISITED, UPDATED, DELETED;

    @Override
    public String getCaption() {
      return BeeUtils.proper(this);
    }
  }

  public enum TaskEvent implements HasCaption {
    ACTIVATED("Vykdoma"),
    SUSPENDED("Sustabdyta"),
    COMPLETED("Įvykdyta"),
    APPROVED("Patvirtinta"),
    CANCELED("Atšaukta"),
    FORWARDED("Persiųsta"),
    EXTENDED("Termino keitimas"),
    RENEWED("Atnaujinta"),
    COMMENTED("Komentaras"),
    VISITED("Peržiūrėta"),
    EDITED("Koreguota"),
    DELETED("Panaikinta");

    private final String caption;

    private TaskEvent(String caption) {
      this.caption = caption;
    }

    @Override
    public String getCaption() {
      return caption;
    }
  }

  public static final String CRM_MODULE = "Crm";
  public static final String CRM_METHOD = CRM_MODULE + "Method";

  public static final String CRM_TASK_PREFIX = "task_";
  public static final String CRM_PROJECT_PREFIX = "project_";

  public static final String VAR_TASK_ID = Service.RPC_VAR_PREFIX + "task_id";
  public static final String VAR_TASK_DATA = Service.RPC_VAR_PREFIX + "task_data";
  public static final String VAR_TASK_COMMENT = Service.RPC_VAR_PREFIX + "task_comment";
  public static final String VAR_TASK_OBSERVE = Service.RPC_VAR_PREFIX + "task_observe";
  public static final String VAR_TASK_DURATION_DATE = Service.RPC_VAR_PREFIX + "task_duration_date";
  public static final String VAR_TASK_DURATION_TIME = Service.RPC_VAR_PREFIX + "task_duration_time";
  public static final String VAR_TASK_DURATION_TYPE = Service.RPC_VAR_PREFIX + "task_duration_type";

  public static final String VAR_PROJECT_ID = Service.RPC_VAR_PREFIX + "project_id";
  public static final String VAR_PROJECT_DATA = Service.RPC_VAR_PREFIX + "project_data";
  public static final String VAR_PROJECT_COMMENT = Service.RPC_VAR_PREFIX + "project_comment";
  public static final String VAR_PROJECT_OBSERVERS = Service.RPC_VAR_PREFIX + "project_observers";

  public static final String SVC_ADD_OBSERVERS = "AddObservers";
  public static final String SVC_REMOVE_OBSERVERS = "RemoveObservers";

  public static final String TBL_TASKS = "Tasks";
  public static final String TBL_TASK_USERS = "TaskUsers";
  public static final String TBL_TASK_EVENTS = "TaskEvents";
  public static final String TBL_TASK_FILES = "TaskFiles";

  public static final String TBL_EVENT_DURATIONS = "EventDurations";

  public static final String TBL_PROJECT_USERS = "ProjectUsers";

  public static final String VIEW_TASKS = "Tasks";
  public static final String VIEW_TASK_TEMPLATES = "TaskTemplates";
  public static final String VIEW_TASK_FILES = "TaskFiles";
  public static final String VIEW_TASK_USERS = "TaskUsers";

  public static final String VIEW_DURATION_TYPES = "DurationTypes";
  
  public static final String COL_START_TIME = "StartTime";
  public static final String COL_FINISH_TIME = "FinishTime";

  public static final String COL_LAST_ACCESS = "LastAccess";
  public static final String COL_LAST_PUBLISH = "LastPublish";

  public static final String COL_FIRST_NAME = "FirstName";
  public static final String COL_LAST_NAME = "LastName";

  public static final String COL_PRIORITY = "Priority";

  public static final String COL_OWNER = "Owner";
  public static final String COL_EXECUTOR = "Executor";

  public static final String COL_PROJECT = "Project";
  public static final String COL_TASK = "Task";
  public static final String COL_USER = "User";

  public static final String COL_NAME = "Name";
  public static final String COL_SUMMARY = "Summary";
  public static final String COL_DESCRIPTION = "Description";
  public static final String COL_CAPTION = "Caption";

  public static final String COL_PARENT = "Parent";
  public static final String COL_ORDER = "Order";

  public static final String COL_CATEGORY = "Category";
  public static final String COL_CATEGORY_NAME = "CategoryName";
  public static final String COL_TYPE = "Type";
  public static final String COL_TYPE_NAME = "TypeName";
  public static final String COL_GROUP = "Group";
  public static final String COL_GROUP_NAME = "GroupName";

  public static final String COL_FILE = "File";
  public static final String COL_FILE_NAME = "FileName";
  public static final String COL_FILE_SIZE = "FileSize";
  public static final String COL_FILE_TYPE = "FileType";
  public static final String COL_FILE_DATE = "FileDate";
  public static final String COL_FILE_VERSION = "FileVersion";

  public static final String COL_DOCUMENT = "Document";
  public static final String COL_DOCUMENT_DATE = "DocumentDate";
  public static final String COL_DOCUMENT_COUNT = "DocumentCount";

  public static final String COL_COMPANY = "Company";
  public static final String COL_CONTACT = "Contact";

  public static final String COL_REMINDER = "Reminder";
  public static final String COL_STATUS = "Status";
  public static final String COL_EXPECTED_DURATION = "ExpectedDuration";

  public static final String COL_PUBLISHER = "Publisher";
  public static final String COL_PUBLISH_TIME = "PublishTime";
  public static final String COL_COMMENT = "Comment";

  public static final String COL_TASK_EVENT = "TaskEvent";

  public static final String COL_DURATION_DATE = "DurationDate";
  public static final String COL_DURATION_TYPE = "DurationType";
  public static final String COL_DURATION = "Duration";

  public static final String COL_EVENT = "Event";
  public static final String COL_EVENT_NOTE = "EventNote";
  public static final String COL_EVENT_DURATION = "EventDuration";

  public static final String COL_STAR = "Star";
  
  public static final String PROP_EXECUTORS = "Executors";
  public static final String PROP_OBSERVERS = "Observers";

  public static final String PROP_COMPANIES = "Companies";
  public static final String PROP_PERSONS = "Persons";
  public static final String PROP_APPOINTMENTS = "Appointments";
  public static final String PROP_TASKS = "Tasks";

  public static final String PROP_FILES = "Files";

  public static final String GRID_TASKS = "Tasks";

  public static final String FORM_NEW_TASK = "NewTask";
  public static final String FORM_TASK = "Task";

  private CrmConstants() {
  }
}
