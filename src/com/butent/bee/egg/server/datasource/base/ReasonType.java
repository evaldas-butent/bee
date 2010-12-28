package com.butent.bee.egg.server.datasource.base;

import com.google.common.collect.Maps;

import java.util.Locale;
import java.util.Map;

public enum ReasonType {
  ACCESS_DENIED,
  USER_NOT_AUTHENTICATED,
  UNSUPPORTED_QUERY_OPERATION,
  INVALID_QUERY,
  INVALID_REQUEST,
  INTERNAL_ERROR,
  NOT_SUPPORTED,
  DATA_TRUNCATED,
  NOT_MODIFIED,
  TIMEOUT,
  ILLEGAL_FORMATTING_PATTERNS,
  OTHER;

  private static final Map<ReasonType, String>
      REASON_TYPE_TO_MESSAGE = Maps.newEnumMap(ReasonType.class);

  static {
    REASON_TYPE_TO_MESSAGE.put(ReasonType.ACCESS_DENIED, "ACCESS_DENIED");
    REASON_TYPE_TO_MESSAGE.put(ReasonType.USER_NOT_AUTHENTICATED, "USER_NOT_AUTHENTICATED");
    REASON_TYPE_TO_MESSAGE.put(ReasonType.UNSUPPORTED_QUERY_OPERATION,
        "UNSUPPORTED_QUERY_OPERATION");
    REASON_TYPE_TO_MESSAGE.put(ReasonType.INVALID_QUERY, "INVALID_QUERY");
    REASON_TYPE_TO_MESSAGE.put(ReasonType.INVALID_REQUEST, "INVALID_REQUEST");
    REASON_TYPE_TO_MESSAGE.put(ReasonType.INTERNAL_ERROR, "INTERNAL_ERROR");
    REASON_TYPE_TO_MESSAGE.put(ReasonType.NOT_SUPPORTED, "NOT_SUPPORTED");
    REASON_TYPE_TO_MESSAGE.put(ReasonType.DATA_TRUNCATED, "DATA_TRUNCATED");
    REASON_TYPE_TO_MESSAGE.put(ReasonType.NOT_MODIFIED, "NOT_MODIFIED");
    REASON_TYPE_TO_MESSAGE.put(ReasonType.TIMEOUT, "TIMEOUT");
    REASON_TYPE_TO_MESSAGE.put(ReasonType.ILLEGAL_FORMATTING_PATTERNS,
        "ILLEGAL_FORMATTING_PATTERNS");
    REASON_TYPE_TO_MESSAGE.put(ReasonType.OTHER, "OTHER");
  }

  public String getMessageForReasonType() {
    return getMessageForReasonType(null);
  }

  public String getMessageForReasonType(Locale locale) {
    return LocaleUtil.getLocalizedMessageFromBundle(
        "com.google.visualization.datasource.base.ErrorMessages", REASON_TYPE_TO_MESSAGE.get(this),
        locale);
  }
  
  public String lowerCaseString() {
    return this.toString().toLowerCase();
  }  
}
