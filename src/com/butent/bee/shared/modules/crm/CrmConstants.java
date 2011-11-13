package com.butent.bee.shared.modules.crm;

import com.butent.bee.shared.Service;

public class CrmConstants {
  public static enum Priority {
    LOW, MEDIUM, HIGH
  }

  public static enum TaskEvent {
    ACTIVATED, SUSPENDED, COMPLETED, APPROVED, CANCELED,
    FORWARDED, EXTENDED, RENEWED, COMMENTED, VISITED
  }

  public static final String CRM_MODULE = "CrmModule";
  public static final String CRM_METHOD = CRM_MODULE + "Method";

  public static final String VAR_TASK_ID = Service.RPC_VAR_PREFIX + "task_id";
  public static final String VAR_TASK_DATA = Service.RPC_VAR_PREFIX + "task_data";
  public static final String VAR_TASK_COMMENT = Service.RPC_VAR_PREFIX + "task_comment";
  public static final String VAR_TASK_OBSERVE = Service.RPC_VAR_PREFIX + "task_observe";
  public static final String VAR_TASK_DURATION_DATE = Service.RPC_VAR_PREFIX + "task_duration_date";
  public static final String VAR_TASK_DURATION_TIME = Service.RPC_VAR_PREFIX + "task_duration_time";
  public static final String VAR_TASK_DURATION_TYPE = Service.RPC_VAR_PREFIX + "task_duration_type";

  public static final String COLUMN_LAST_ACCESS = "LastAccess";
}
