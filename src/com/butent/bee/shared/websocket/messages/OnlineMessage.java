package com.butent.bee.shared.websocket.messages;

import com.butent.bee.shared.utils.BeeUtils;
import com.butent.bee.shared.utils.Codec;
import com.butent.bee.shared.websocket.SessionUser;

import java.util.ArrayList;
import java.util.List;

public class OnlineMessage extends Message {

  private final List<SessionUser> sessionUsers = new ArrayList<>();

  public OnlineMessage(List<SessionUser> sessionUsers) {
    this();

    if (!BeeUtils.isEmpty(sessionUsers)) {
      this.sessionUsers.addAll(sessionUsers);
    }
  }

  OnlineMessage() {
    super(Type.ONLINE);
  }

  @Override
  public String brief() {
    return BeeUtils.toString(getSessionUsers().size());
  }

  public List<SessionUser> getSessionUsers() {
    return sessionUsers;
  }

  @Override
  public boolean isValid() {
    return !BeeUtils.isEmpty(getSessionUsers());
  }

  @Override
  public String toString() {
    return BeeUtils.joinOptions("type", string(getType()),
        "session users", getSessionUsers().isEmpty() ? null : getSessionUsers().toString());
  }

  @Override
  protected void deserialize(String s) {
    if (!sessionUsers.isEmpty()) {
      sessionUsers.clear();
    }

    String[] arr = Codec.beeDeserializeCollection(s);
    if (arr != null) {
      for (String su : arr) {
        sessionUsers.add(SessionUser.restore(su));
      }
    }
  }

  @Override
  protected String serialize() {
    return Codec.beeSerialize(getSessionUsers());
  }
}
