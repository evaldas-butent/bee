package com.butent.bee.egg.server.datasource.base;

@SuppressWarnings("serial")
public class DataSourceException extends Exception {
  private ReasonType reasonType;
  private String messageToUser = null;
  
  public DataSourceException(ReasonType reasonType, String messageToUser) {
    super(messageToUser);
    this.messageToUser = messageToUser;
    this.reasonType = reasonType;
  }

  protected DataSourceException() {
  }

  public String getMessageToUser() {
    return messageToUser;
  }

  public ReasonType getReasonType() {
    return reasonType;
  }
}
