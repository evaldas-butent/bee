package com.butent.bee.shared.modules.crm;

import com.butent.bee.shared.Service;

public class CrmConstants {
  public static enum Priority {
    LOW, MEDIUM, HIGH
  }

  public static enum TaskEvent {
    ACTIVATED, SUSPENDED, COMPLETED, APPROVED, CANCELED,
    FORWARDED, EXTENDED, RENEWED, COMMENTED
  }

  public static final String CRM_MODULE = "CrmModule";
  public static final String CRM_METHOD = CRM_MODULE + "Method";

  public static final String VAR_TASK_ID = Service.RPC_VAR_PREFIX + "task_id";
  public static final String VAR_TASK_DATA = Service.RPC_VAR_PREFIX + "task_data";
  public static final String VAR_TASK_TERM = Service.RPC_VAR_PREFIX + "task_term";
  public static final String VAR_TASK_COMMENT = Service.RPC_VAR_PREFIX + "task_comment";
}
