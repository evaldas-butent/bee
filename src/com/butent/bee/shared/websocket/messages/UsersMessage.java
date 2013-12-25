package com.butent.bee.shared.websocket.messages;

import com.google.common.collect.Lists;

import com.butent.bee.shared.utils.BeeUtils;
import com.butent.bee.shared.utils.Codec;
import com.butent.bee.shared.websocket.SessionUser;

import java.util.List;

public class UsersMessage extends Message {
  
  private final List<SessionUser> users = Lists.newArrayList();

  public UsersMessage(List<SessionUser> users) {
    this();
    this.users.addAll(users);
  }

  UsersMessage() {
    super(Type.USERS);
  }
  
  public List<SessionUser> getUsers() {
    return users;
  }

  @Override
  public String toString() {
    return BeeUtils.joinOptions("type", string(getType()),
        "users", BeeUtils.isEmpty(getUsers()) ? null : getUsers().toString());
  }
  
  @Override
  protected void deserialize(String s) {
    if (!users.isEmpty()) {
      users.clear();
    }

    String[] arr = Codec.beeDeserializeCollection(s);
    if (arr != null) {
      for (String su : arr) {
        users.add(SessionUser.restore(su));
      }
    }
  }

  @Override
  protected String serialize() {
    return Codec.beeSerialize(users);
  }
}
