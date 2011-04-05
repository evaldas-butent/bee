package com.butent.bee.shared.data;

public class DataWarning {
  private String reason;
  private String message;

  public DataWarning(String reason, String message) {
    this.reason = reason;
    this.message = message;
  }

  public String getMessage() {
    return message;
  }

  public String getReason() {
    return reason;
  }
}
