package com.butent.bee.client;

import com.butent.bee.shared.utils.BeeUtils;

public class BeeUser implements Module {

  private String sessionId = null;
  private String userSign = null;

  public void end() {
  }

  public String getName() {
    return getClass().getName();
  }

  public int getPriority(int p) {
    switch (p) {
      case PRIORITY_INIT:
        return DO_NOT_CALL;
      case PRIORITY_START:
        return DO_NOT_CALL;
      case PRIORITY_END:
        return DO_NOT_CALL;
      default:
        return DO_NOT_CALL;
    }
  }

  public String getSessionId() {
    return sessionId;
  }

  public String getUserSign() {
    return userSign;
  }

  public void init() {
  }
  
  public boolean isLoggedIn() {
    return !BeeUtils.isEmpty(getUserSign());
  }

  public void setSessionId(String sessionId) {
    this.sessionId = sessionId;
  }

  public void setUserSign(String userSign) {
    this.userSign = userSign;
  }

  public void start() {
  }
}
