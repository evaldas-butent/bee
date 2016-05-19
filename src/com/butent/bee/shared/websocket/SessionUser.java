package com.butent.bee.shared.websocket;

import com.google.common.collect.Lists;

import com.butent.bee.shared.Assert;
import com.butent.bee.shared.BeeSerializable;
import com.butent.bee.shared.communication.Presence;
import com.butent.bee.shared.data.DataUtils;
import com.butent.bee.shared.utils.BeeUtils;
import com.butent.bee.shared.utils.Codec;

import java.util.List;

public class SessionUser implements BeeSerializable {

  public static SessionUser restore(String s) {
    Assert.notEmpty(s);

    SessionUser sessionUser = new SessionUser();
    sessionUser.deserialize(s);
    return sessionUser;
  }

  private String sessionId;
  private long userId;
  private Presence presence;

  public SessionUser(String sessionId, long userId, Presence presence) {
    this.sessionId = sessionId;
    this.userId = userId;
    this.presence = presence;
  }

  private SessionUser() {
  }

  @Override
  public void deserialize(String s) {
    String[] arr = Codec.beeDeserializeCollection(s);
    Assert.lengthEquals(arr, 3);

    setSessionId(arr[0]);
    setUserId(BeeUtils.toLong(arr[1]));
    setPresence(Codec.unpack(Presence.class, arr[2]));
  }

  public Presence getPresence() {
    return presence;
  }

  public String getSessionId() {
    return sessionId;
  }

  public long getUserId() {
    return userId;
  }

  public boolean isValid() {
    return !BeeUtils.isEmpty(getSessionId())
        && DataUtils.isId(getUserId())
        && getPresence() != null;
  }

  @Override
  public String serialize() {
    List<String> values = Lists.newArrayList(getSessionId(), BeeUtils.toString(getUserId()),
        Codec.pack(getPresence()));
    return Codec.beeSerialize(values);
  }

  @Override
  public String toString() {
    return BeeUtils.joinOptions("sessionId", getSessionId(), "userId", getUserId(),
        "presence", getPresence());
  }

  private void setPresence(Presence presence) {
    this.presence = presence;
  }

  private void setSessionId(String sessionId) {
    this.sessionId = sessionId;
  }

  private void setUserId(long userId) {
    this.userId = userId;
  }
}
