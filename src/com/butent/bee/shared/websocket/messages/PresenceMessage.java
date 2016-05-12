package com.butent.bee.shared.websocket.messages;

import com.butent.bee.shared.communication.Presence;
import com.butent.bee.shared.utils.BeeUtils;
import com.butent.bee.shared.utils.EnumUtils;
import com.butent.bee.shared.websocket.SessionUser;

public class PresenceMessage extends Message {

  private SessionUser sessionUser;

  public PresenceMessage(String sessionId, long userId, Presence presence) {
    this(new SessionUser(sessionId, userId, presence));
  }

  public PresenceMessage(SessionUser sessionUser) {
    this();

    this.sessionUser = sessionUser;
  }

  PresenceMessage() {
    super(Type.PRESENCE);
  }

  @Override
  public String brief() {
    if (getSessionUser() == null) {
      return null;
    } else {
      return BeeUtils.joinWords(getSessionUser().getUserId(),
          EnumUtils.toLowerCase(getSessionUser().getPresence()));
    }
  }

  public SessionUser getSessionUser() {
    return sessionUser;
  }

  @Override
  public boolean isValid() {
    return getSessionUser() != null && getSessionUser().isValid();
  }

  @Override
  public String toString() {
    return BeeUtils.joinOptions("type", string(getType()), "sessionUser",
        (getSessionUser() == null) ? null : BeeUtils.bracket(getSessionUser().toString()));
  }

  @Override
  protected void deserialize(String s) {
    this.sessionUser = SessionUser.restore(s);
  }

  @Override
  protected String serialize() {
    return (getSessionUser() == null) ? null : getSessionUser().serialize();
  }
}
