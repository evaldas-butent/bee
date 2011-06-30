package com.butent.bee.client;

import com.butent.bee.shared.data.UserData;

/**
 * gets user login status, session ID and stores them.
 * 
 * 
 */

public class UserInfo implements Module {

  private String sessionId = null;
  private UserData userData = null;

  public void end() {
  }

  public String getLogin() {
    if (isLoggedIn()) {
      return userData.getLogin();
    }
    return null;
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
    if (isLoggedIn()) {
      return userData.getUserSign();
    }
    return null;
  }

  public void init() {
  }

  public boolean isLoggedIn() {
    return userData != null;
  }

  public void setSessionId(String sessionId) {
    this.sessionId = sessionId;
  }

  public void setUserData(UserData userData) {
    this.userData = userData;
  }

  public void start() {
  }
}
