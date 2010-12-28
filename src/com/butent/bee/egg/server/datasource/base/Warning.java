package com.butent.bee.egg.server.datasource.base;

public class Warning {

  private ReasonType reasonType;
  private String messageToUser;

  public Warning(ReasonType reasonType, String messageToUser) {
    this.messageToUser = messageToUser;
    this.reasonType = reasonType;
  }

  public String getMessage() {
    return messageToUser;
  }

  public ReasonType getReasonType() {
    return reasonType;
  }
}
