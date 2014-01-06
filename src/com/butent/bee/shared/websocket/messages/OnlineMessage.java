package com.butent.bee.shared.websocket.messages;

import com.google.common.collect.Lists;

import com.butent.bee.shared.utils.BeeUtils;
import com.butent.bee.shared.utils.Codec;
import com.butent.bee.shared.websocket.SessionUser;

import java.util.List;

public class OnlineMessage extends Message {
  
  private final List<SessionUser> sessionUsers = Lists.newArrayList();

  public OnlineMessage(List<SessionUser> sessionUsers) {
    this();
    this.sessionUsers.addAll(sessionUsers);
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
  public String toString() {
    return BeeUtils.joinOptions("type", string(getType()),
        "session users", BeeUtils.isEmpty(getSessionUsers()) ? null : getSessionUsers().toString());
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
