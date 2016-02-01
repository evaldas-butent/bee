package com.butent.bee.shared.websocket.messages;

import com.butent.bee.shared.Pair;
import com.butent.bee.shared.communication.Presence;
import com.butent.bee.shared.utils.BeeUtils;
import com.butent.bee.shared.utils.Codec;
import com.butent.bee.shared.websocket.SessionUser;

public class PresenceMessage extends Message {

  public static PresenceMessage away(SessionUser su) {
    return new PresenceMessage(su, Presence.AWAY);
  }

  public static PresenceMessage idle(SessionUser su) {
    return new PresenceMessage(su, Presence.IDLE);
  }

  public static PresenceMessage offline(SessionUser su) {
    return new PresenceMessage(su, Presence.OFFLINE);
  }

  public static PresenceMessage online(SessionUser su) {
    return new PresenceMessage(su, Presence.ONLINE);
  }

  private SessionUser sessionUser;
  private Presence presence;

  private PresenceMessage(SessionUser sessionUser, Presence presence) {
    this();

    this.sessionUser = sessionUser;
    this.presence = presence;
  }

  PresenceMessage() {
    super(Type.PRESENCE);
  }

  @Override
  public String brief() {
    return string(getPresence());
  }

  public Presence getPresence() {
    return presence;
  }

  public SessionUser getSessionUser() {
    return sessionUser;
  }

  public boolean isOffline() {
    return getPresence() == Presence.OFFLINE;
  }

  public boolean isOnline() {
    return getPresence() == Presence.ONLINE;
  }

  @Override
  public boolean isValid() {
    return getSessionUser() != null && getPresence() != null;
  }

  @Override
  public String toString() {
    return BeeUtils.joinOptions("type", string(getType()), "sessionUser",
        (getSessionUser() == null) ? null : BeeUtils.bracket(getSessionUser().toString()),
        "presence", string(getPresence()));
  }

  @Override
  protected void deserialize(String s) {
    Pair<String, String> pair = Pair.restore(s);

    this.sessionUser = SessionUser.restore(pair.getA());
    this.presence = Codec.unpack(Presence.class, pair.getB());
  }

  @Override
  protected String serialize() {
    return Pair.of(getSessionUser(), Codec.pack(getPresence())).serialize();
  }
}
