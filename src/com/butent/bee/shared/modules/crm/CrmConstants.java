package com.butent.bee.shared.modules.crm;

import com.butent.bee.shared.Service;

public class CrmConstants {
  public static enum Priority {
    LOW, MEDIUM, HIGH
  }

  public static enum ProjectEvent {
    CREATED, ACTIVATED, SUSPENDED, COMPLETED, CANCELED,
    EXTENDED, RENEWED, COMMENTED, VISITED, UPDATED, DELETED
  }

  public static enum TaskEvent {
    ACTIVATED, SUSPENDED, COMPLETED, APPROVED, CANCELED,
    FORWARDED, EXTENDED, RENEWED, COMMENTED, VISITED, UPDATED, DELETED
  }

  public static final String CRM_MODULE = "CrmModule";
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
  public static final String VAR_TASK_EXECUTORS = Service.RPC_VAR_PREFIX + "task_executors";
  public static final String VAR_TASK_OBSERVERS = Service.RPC_VAR_PREFIX + "task_observers";

  public static final String VAR_PROJECT_ID = Service.RPC_VAR_PREFIX + "project_id";
  public static final String VAR_PROJECT_DATA = Service.RPC_VAR_PREFIX + "project_data";
  public static final String VAR_PROJECT_COMMENT = Service.RPC_VAR_PREFIX + "project_comment";
  public static final String VAR_PROJECT_OBSERVERS = Service.RPC_VAR_PREFIX + "project_observers";

  public static final String SVC_ADD_OBSERVERS = "AddObservers";
  public static final String SVC_REMOVE_OBSERVERS = "RemoveObservers";
  public static final String SVC_ADD_STAGE = "AddStage";
  public static final String SVC_REMOVE_STAGE = "RemoveStage";
  public static final String SVC_EDIT_STAGE = "EditStage";

  public static final String TBL_TASK_USERS = "TaskUsers";
  public static final String TBL_PROJECT_USERS = "ProjectUsers";
  public static final String TBL_PROJECT_STAGES = "ProjectStages";

  public static final String COL_START_TIME = "StartTime";
  public static final String COL_FINISH_TIME = "FinishTime";

  public static final String COL_LAST_ACCESS = "LastAccess";
  public static final String COL_LAST_PUBLISH = "LastPublish";

  public static final String COL_FIRST_NAME = "FirstName";
  public static final String COL_LAST_NAME = "LastName";

  public static final String COL_USER_ID = "UserID";
  public static final String COL_EVENT = "Event";
  public static final String COL_PRIORITY = "Priority";

  public static final String COL_OWNER = "Owner";
  public static final String COL_EXECUTOR = "Executor";

  public static final String COL_PROJECT = "Project";
  public static final String COL_TASK = "Task";
  public static final String COL_USER = "User";

  public static final String COL_NAME = "Name";
  public static final String COL_PARENT = "Parent";
  public static final String COL_ORDER = "Order";

  public static final String COL_CATEGORY = "Category";
}
