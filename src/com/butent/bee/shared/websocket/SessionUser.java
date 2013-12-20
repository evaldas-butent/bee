package com.butent.bee.shared.websocket;

import com.butent.bee.shared.Assert;
import com.butent.bee.shared.BeeSerializable;
import com.butent.bee.shared.utils.BeeUtils;
import com.butent.bee.shared.utils.Codec;

public class SessionUser implements BeeSerializable {
  
  private enum Serial {
    LOGIN, USER_ID, SESSION_ID
  }
  
  public static SessionUser restore(String s) {
    Assert.notEmpty(s);
    
    SessionUser sessionUser = new SessionUser();
    sessionUser.deserialize(s);
    return sessionUser;
  }
  
  private String login;
  private long userId;

  private String sessionId;

  private SessionUser() {
  }

  public SessionUser(String login, long userId, String sessionId) {
    this.login = login;
    this.userId = userId;
    this.sessionId = sessionId;
  }

  @Override
  public void deserialize(String s) {
    String[] arr = Codec.beeDeserializeCollection(s);
    Serial[] members = Serial.values();
    Assert.lengthEquals(arr, members.length);

    for (int i = 0; i < members.length; i++) {
      Serial member = members[i];
      String value = arr[i];
      if (value == null) {
        continue;
      }

      switch (member) {
        case LOGIN:
          this.login = value;
          break;
        case USER_ID:
          this.userId = BeeUtils.toLong(value);
          break;
        case SESSION_ID:
          this.sessionId = value;
          break;
      }
    }
  }

  public String getLogin() {
    return login;
  }

  public String getSessionId() {
    return sessionId;
  }

  public long getUserId() {
    return userId;
  }

  @Override
  public String serialize() {
    Serial[] members = Serial.values();
    Object[] arr = new Object[members.length];
    int i = 0;

    for (Serial member : members) {
      switch (member) {
        case LOGIN:
          arr[i++] = getLogin();
          break;
        case USER_ID:
          arr[i++] = getUserId();
          break;
        case SESSION_ID:
          arr[i++] = getSessionId();
          break;
      }
    }
    return Codec.beeSerialize(arr);
  }
}
