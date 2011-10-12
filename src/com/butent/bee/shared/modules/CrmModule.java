package com.butent.bee.shared.modules;

public class CrmModule {
  public static enum Priority {
    LOW, MEDIUM, HIGH
  }

  public static enum TaskEvent {
    ACTIVATED, SUSPENDED, COMPLETED, APPROVED, CANCELED,
    FORWARDED, EXTENDED, RENEWED, COMMENTED
  }
}
