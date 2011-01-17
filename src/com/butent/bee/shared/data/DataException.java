package com.butent.bee.shared.data;

import com.butent.bee.shared.exceptions.BeeException;

@SuppressWarnings("serial")
public class DataException extends BeeException {
  private Reasons reasonType;
  private String messageToUser = null;
  
  public DataException(Reasons reasonType, String messageToUser) {
    super(messageToUser);
    this.messageToUser = messageToUser;
    this.reasonType = reasonType;
  }

  protected DataException() {
  }

  public String getMessageToUser() {
    return messageToUser;
  }

  public Reasons getReasonType() {
    return reasonType;
  }
}
