package com.butent.bee.egg.shared.data;

public class DataWarning {
  private Reasons reasonType;
  private String messageToUser;

  public DataWarning(Reasons reasonType, String messageToUser) {
    this.messageToUser = messageToUser;
    this.reasonType = reasonType;
  }

  public String getMessage() {
    return messageToUser;
  }

  public Reasons getReasonType() {
    return reasonType;
  }
}
