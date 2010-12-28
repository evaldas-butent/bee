package com.butent.bee.egg.server.datasource.base;

public class ResponseStatus {
  public static final String SIGN_IN_MESSAGE_KEY = "SIGN_IN";

  public static ResponseStatus createResponseStatus(DataSourceException dse) {
    return new ResponseStatus(StatusType.ERROR, dse.getReasonType(), dse.getMessageToUser());
  }
  public static ResponseStatus getModifiedResponseStatus(ResponseStatus responseStatus) {
    String signInString = LocaleUtil.getLocalizedMessageFromBundle(
        "com.google.visualization.datasource.base.ErrorMessages", SIGN_IN_MESSAGE_KEY, null);
    if (responseStatus.getReasonType() == ReasonType.USER_NOT_AUTHENTICATED) {
      String msg = responseStatus.getDescription();
      if (!msg.contains(" ") && (msg.startsWith("http://") || msg.startsWith("https://"))) {
        StringBuilder sb = new StringBuilder("<a target=\"_blank\" href=\"")
            .append(msg).append("\">")
            .append(signInString)
            .append("</a>");
        responseStatus = new ResponseStatus(responseStatus.getStatusType(),
            responseStatus.getReasonType(), sb.toString());
      }
    }
    return responseStatus;
  }
  private StatusType statusType;

  private ReasonType reasonType;

  private String description;

  public ResponseStatus(StatusType statusType) {
    this(statusType, null, null);
  }

  public ResponseStatus(StatusType statusType, ReasonType reasonType, String description) {
    this.statusType = statusType;
    this.reasonType = reasonType;
    this.description = description;
  }

  public String getDescription() {
    return description;
  }

  public ReasonType getReasonType() {
    return reasonType;
  }

  public StatusType getStatusType() {
    return statusType;
  }
}
