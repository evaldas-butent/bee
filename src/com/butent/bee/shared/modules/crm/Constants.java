package com.butent.bee.shared.modules.crm;

public class Constants {
  public static enum Priority {
    LOW, MEDIUM, HIGH
  }

  public static enum TaskEvent {
    ACTIVATED, SUSPENDED, COMPLETED, APPROVED, CANCELED,
    FORWARDED, EXTENDED, RENEWED, COMMENTED
  }
}
