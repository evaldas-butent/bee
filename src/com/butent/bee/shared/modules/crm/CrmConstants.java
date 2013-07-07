package com.butent.bee.shared.modules.crm;

import com.butent.bee.shared.Service;
import com.butent.bee.shared.i18n.Localized;
import com.butent.bee.shared.ui.HasCaption;

public final class CrmConstants {

  public enum TaskEvent implements HasCaption {
    CREATE("Sukurta", null),
    VISIT("Peržiūrėta", null),
    ACTIVATE("Perduota vykdymui", "Perduoti vykdymui"),
    COMMENT("Komentaras", "Komentuoti"),
    EXTEND("Pratęsta", "Keisti terminą"),
    SUSPEND("Sustabdyta", "Sustabdyti"),
    RENEW("Atnaujinta", "Grąžinti vykdymui"),
    FORWARD("Persiųsta", "Persiųsti"),
    CANCEL("Nutraukta", "Nutraukti"),
    COMPLETE("Įvykdyta", "Užbaigti"),
    APPROVE("Patvirtinta", "Patvirtinti"),
    EDIT("Koreguota", null);

    private final String caption;
    private final String commandLabel;

    private TaskEvent(String caption, String commandLabel) {
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

  public enum TaskPriority implements HasCaption {
    LOW("Žemas"), MEDIUM("Vidutinis"), HIGH("Aukštas");

    private final String caption;

    private TaskPriority(String caption) {
      this.caption = caption;
    }

    @Override
    public String getCaption() {
      return caption;
    }
  }

  public enum TaskStatus implements HasCaption {
    NOT_VISITED(Localized.getConstants().taskStatusNotVisited()),
    ACTIVE(Localized.getConstants().taskStatusActive()),
    SCHEDULED(Localized.getConstants().taskStatusScheduled()),
    SUSPENDED(Localized.getConstants().taskStatusSuspended()),
    COMPLETED(Localized.getConstants().taskStatusCompleted()),
    APPROVED(Localized.getConstants().taskStatusApproved()),
    CANCELED(Localized.getConstants().taskStatusCanceled());

    public static boolean in(int status, TaskStatus... statuses) {
      for (TaskStatus ts : statuses) {
        if (ts.ordinal() == status) {
          return true;
        }
      }
      return false;
    }

    private final String caption;

    private TaskStatus(String caption) {
      this.caption = caption;
    }

    @Override
    public String getCaption() {
      return caption;
    }

    public boolean is(Integer status) {
      return status != null && ordinal() == status;
    }
  }

  public static final String CRM_MODULE = "Crm";
  public static final String CRM_METHOD = CRM_MODULE + "Method";

  public static final String CRM_TASK_PREFIX = "task_";

  public static final String SVC_GET_TASK_DATA = "get_task_data";
  public static final String SVC_GET_CHANGED_TASKS = "get_changed_tasks";
  public static final String SVC_TASKS_REPORTS_PREFIX = "get_tasks_reports_";
  public static final String SVC_TASKS_REPORTS_COMPANY_TIMES = SVC_TASKS_REPORTS_PREFIX
      + "company_times";
  public static final String SVC_TASKS_REPORTS_TYPE_HOURS = SVC_TASKS_REPORTS_PREFIX + "type_hours";
  public static final String SVC_TASKS_REPORTS_USERS_HOURS = SVC_TASKS_REPORTS_PREFIX
      + "users_hours";

  public static final String SVC_GET_REQUEST_FILES = "get_request_files";

  public static final String VAR_TASK_DATA = Service.RPC_VAR_PREFIX + "task_data";
  public static final String VAR_TASK_ID = Service.RPC_VAR_PREFIX + "task_id";

  public static final String VAR_TASK_COMMENT = Service.RPC_VAR_PREFIX + "task_comment";
  public static final String VAR_TASK_NOTES = Service.RPC_VAR_PREFIX + "task_notes";
  public static final String VAR_TASK_FINISH_TIME = Service.RPC_VAR_PREFIX + "task_finish_time";
  public static final String VAR_TASK_PUBLISHER = Service.RPC_VAR_PREFIX + "task_publisher";
  public static final String VAR_TASK_COMPANY = Service.RPC_VAR_PREFIX + "task_company";

  public static final String VAR_TASK_DURATION_DATE = Service.RPC_VAR_PREFIX + "task_duration_date";
  public static final String VAR_TASK_DURATION_TIME = Service.RPC_VAR_PREFIX + "task_duration_time";
  public static final String VAR_TASK_DURATION_TYPE = Service.RPC_VAR_PREFIX + "task_duration_type";
  public static final String VAR_TASK_DURATION_DATE_FROM = VAR_TASK_DURATION_DATE + "_from";
  public static final String VAR_TASK_DURATION_DATE_TO = VAR_TASK_DURATION_DATE + "_to";
  public static final String VAR_TASK_DURATION_HIDE_ZEROS = Service.RPC_VAR_PREFIX
      + "task_duration_hide_zeros";

  public static final String VAR_TASK_RELATIONS = Service.RPC_VAR_PREFIX + "task_relations";
  public static final String VAR_TASK_USERS = Service.RPC_VAR_PREFIX + "task_users";

  public static final String TBL_REQUESTS = "Requests";
  public static final String TBL_REQUEST_FILES = "RequestFiles";

  public static final String TBL_TASKS = "Tasks";
  public static final String TBL_TASK_USERS = "TaskUsers";
  public static final String TBL_TASK_EVENTS = "TaskEvents";
  public static final String TBL_TASK_FILES = "TaskFiles";
  public static final String TBL_DURATION_TYPES = "DurationTypes";

  public static final String TBL_EVENT_DURATIONS = "EventDurations";

  public static final String VIEW_TASKS = "Tasks";
  public static final String VIEW_TASK_TEMPLATES = "TaskTemplates";
  public static final String VIEW_TASK_FILES = "TaskFiles";
  public static final String VIEW_TASK_USERS = "TaskUsers";
  public static final String VIEW_TASK_EVENTS = "TaskEvents";
  public static final String VIEW_TASK_DURATIONS = "TaskDurations";

  public static final String VIEW_DURATION_TYPES = "DurationTypes";

  public static final String VIEW_RECURRING_TASKS = "RecurringTasks";

  public static final String VIEW_DOCUMENTS = "Documents";
  public static final String VIEW_DOCUMENT_FILES = "DocumentFiles";

  public static final String COL_START_TIME = "StartTime";
  public static final String COL_FINISH_TIME = "FinishTime";

  public static final String COL_FIRST_NAME = "FirstName";
  public static final String COL_LAST_NAME = "LastName";

  public static final String COL_PRIORITY = "Priority";

  public static final String COL_OWNER = "Owner";

  public static final String COL_EXECUTOR = "Executor";
  public static final String COL_EXECUTOR_FIRST_NAME = "ExecutorFirstName";
  public static final String COL_EXECUTOR_LAST_NAME = "ExecutorLastName";

  public static final String COL_TASK = "Task";
  public static final String COL_USER = "User";

  public static final String COL_NAME = "Name";
  public static final String COL_SUMMARY = "Summary";
  public static final String COL_DESCRIPTION = "Description";
  public static final String COL_CAPTION = "Caption";

  public static final String COL_PARENT = "Parent";
  public static final String COL_ORDER = "Order";

  public static final String COL_DOCUMENT = "Document";
  public static final String COL_DOCUMENT_DATE = "DocumentDate";
  public static final String COL_DOCUMENT_COUNT = "DocumentCount";
  public static final String COL_DOCUMENT_CATEGORY = "Category";
  public static final String COL_DOCUMENT_CATEGORY_NAME = "CategoryName";
  public static final String COL_DOCUMENT_TYPE = "Type";
  public static final String COL_DOCUMENT_TYPE_NAME = "TypeName";
  public static final String COL_DOCUMENT_PLACE = "Place";
  public static final String COL_DOCUMENT_PLACE_NAME = "PlaceName";
  public static final String COL_DOCUMENT_STATUS = "Status";
  public static final String COL_DOCUMENT_STATUS_NAME = "StatusName";

  public static final String COL_FILE = "File";
  public static final String COL_FILE_NAME = "FileName";
  public static final String COL_FILE_SIZE = "FileSize";
  public static final String COL_FILE_TYPE = "FileType";
  public static final String COL_FILE_DATE = "FileDate";
  public static final String COL_FILE_VERSION = "FileVersion";

  public static final String COL_NUMBER = "Number";
  public static final String COL_REGISTRATION_NUMBER = "RegistrationNumber";
  public static final String COL_EXPIRES = "Expires";

  public static final String COL_COMPANY = "Company";
  public static final String COL_COMPANY_NAME = "CompanyName";
  public static final String COL_CONTACT = "Contact";
  public static final String COL_CONTACT_FIRST_NAME = "ContactFirstName";
  public static final String COL_CONTACT_LAST_NAME = "ContactLastName";

  public static final String COL_REMINDER = "Reminder";
  public static final String COL_STATUS = "Status";
  public static final String COL_EXPECTED_DURATION = "ExpectedDuration";

  public static final String COL_PUBLISH_TIME = "PublishTime";
  public static final String COL_PUBLISHER = "Publisher";
  public static final String COL_PUBLISHER_FIRST_NAME = "PublisherFirstName";
  public static final String COL_PUBLISHER_LAST_NAME = "PublisherLastName";

  public static final String COL_COMMENT = "Comment";

  public static final String COL_TASK_EVENT = "TaskEvent";

  public static final String COL_DURATION_DATE = "DurationDate";
  public static final String COL_DURATION_TYPE = "DurationType";
  public static final String COL_DURATION = "Duration";

  public static final String COL_EVENT = "Event";
  public static final String COL_EVENT_NOTE = "EventNote";
  public static final String COL_EVENT_DURATION = "EventDuration";

  public static final String COL_LAST_ACCESS = "LastAccess";
  public static final String COL_STAR = "Star";

  public static final String COL_COMPLETED = "Completed";
  public static final String COL_APPROVED = "Approved";

  public static final String COL_PERSON = "Person";
  public static final String COL_PERSON_FIRST_NAME = "PersonFirstName";
  public static final String COL_PERSON_LAST_NAME = "PersonLastName";
  public static final String COL_PERSON_COMPANY_NAME = "PersonCompanyName";

  public static final String COL_REQUEST = "Request";
  public static final String COL_REQUEST_DATE = "Date";
  public static final String COL_REQUEST_MANAGER = "Manager";
  public static final String COL_REQUEST_RESULT = "Result";
  public static final String COL_REQUEST_FINISHED = "Finished";

  public static final String PROP_EXECUTORS = "Executors";
  public static final String PROP_OBSERVERS = "Observers";

  public static final String PROP_COMPANIES = "Companies";
  public static final String PROP_PERSONS = "Persons";
  public static final String PROP_APPOINTMENTS = "Appointments";
  public static final String PROP_TASKS = "Tasks";

  public static final String PROP_FILES = "Files";
  public static final String PROP_EVENTS = "Events";

  public static final String PROP_USER = "User";
  public static final String PROP_STAR = "Star";
  public static final String PROP_LAST_ACCESS = "LastAccess";
  public static final String PROP_LAST_PUBLISH = "LastPublish";

  public static final String PROP_LAST_EVENT_ID = "LastEventId";

  public static final String PROP_ICON = "Icon";

  public static final String GRID_TASKS = "Tasks";
  public static final String GRID_TASKS_TYPE_HOURS_REPORT = "TasksTypeHoursReport";

  public static final String GRID_REQUESTS = "Requests";

  public static final String FORM_NEW_TASK = "NewTask";
  public static final String FORM_TASK = "Task";
  public static final String FORM_TASKS_REPORT = "TasksReport";

  public static final String FORM_NEW_REQUEST = "NewRequest";
  public static final String FORM_REQUEST = "Request";

  public static final String CRM_STYLE_PREFIX = "bee-crm-";

  public static final String LABEL_OBSERVERS = "Stebėtojai";

  private CrmConstants() {
  }
}
