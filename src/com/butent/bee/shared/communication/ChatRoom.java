package com.butent.bee.shared.communication;

import com.butent.bee.shared.Assert;
import com.butent.bee.shared.BeeConst;
import com.butent.bee.shared.BeeSerializable;
import com.butent.bee.shared.HasInfo;
import com.butent.bee.shared.data.DataUtils;
import com.butent.bee.shared.time.TimeUtils;
import com.butent.bee.shared.utils.BeeUtils;
import com.butent.bee.shared.utils.Codec;
import com.butent.bee.shared.utils.Property;
import com.butent.bee.shared.utils.PropertyUtils;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Objects;

public class ChatRoom implements BeeSerializable, HasInfo, Comparable<ChatRoom> {

  private enum Serial {
    ID, NAME, USERS, CREATED, CREATOR, REGISTERED, LAST_ACCESS,
    MESSAGES, MESSAGE_COUNT, UNREAD_COUNT, LAST_MESSAGE
  }

  public static ChatRoom restore(String s) {
    ChatRoom room = new ChatRoom();
    room.deserialize(s);
    return room;
  }

  private static String formatMillis(long millis) {
    return (millis > 0) ? TimeUtils.renderDateTime(millis, true) : null;
  }

  private long id;
  private String name;

  private final List<Long> users = new ArrayList<>();

  private long created;
  private long creator;

  private long registered;
  private long lastAccess;

  private final List<ChatItem> messages = new ArrayList<>();

  private int messageCount;
  private int unreadCount;

  private ChatItem lastMessage;

  public ChatRoom(long id, String name) {
    this.id = id;
    this.name = name;
  }

  private ChatRoom() {
  }

  @Override
  public int compareTo(ChatRoom o) {
    int result = Boolean.compare(o.unreadCount > 0, unreadCount > 0);

    if (result == BeeConst.COMPARE_EQUAL) {
      result = Long.compare(o.getMaxTime(), getMaxTime());
    }
    if (result == BeeConst.COMPARE_EQUAL) {
      result = Long.compare(o.created, created);
    }

    return result;
  }

  @Override
  public void deserialize(String s) {
    String[] arr = Codec.beeDeserializeCollection(s);
    Assert.lengthEquals(arr, Serial.values().length);

    for (int i = 0; i < arr.length; i++) {
      String value = arr[i];
      if (BeeUtils.isEmpty(value)) {
        continue;
      }

      switch (Serial.values()[i]) {
        case ID:
          setId(BeeUtils.toLong(value));
          break;

        case NAME:
          setName(value);
          break;

        case USERS:
          if (!users.isEmpty()) {
            users.clear();
          }

          String[] uArr = Codec.beeDeserializeCollection(value);
          if (uArr != null) {
            for (String us : uArr) {
              Long user = BeeUtils.toLongOrNull(us);
              if (DataUtils.isId(user)) {
                users.add(user);
              }
            }
          }
          break;

        case CREATED:
          setCreated(BeeUtils.toLong(value));
          break;

        case CREATOR:
          setCreator(BeeUtils.toLong(value));
          break;

        case REGISTERED:
          setRegistered(BeeUtils.toLong(value));
          break;

        case LAST_ACCESS:
          setLastAccess(BeeUtils.toLong(value));
          break;

        case MESSAGES:
          if (!messages.isEmpty()) {
            messages.clear();
          }

          String[] mArr = Codec.beeDeserializeCollection(value);
          if (mArr != null) {
            for (String msg : mArr) {
              messages.add(ChatItem.restore(msg));
            }
          }
          break;

        case MESSAGE_COUNT:
          setMessageCount(BeeUtils.toInt(value));
          break;

        case UNREAD_COUNT:
          setUnreadCount(BeeUtils.toInt(value));
          break;

        case LAST_MESSAGE:
          setLastMessage(ChatItem.restore(value));
          break;
      }
    }
  }

  public long getCreated() {
    return created;
  }

  public long getCreator() {
    return creator;
  }

  public long getId() {
    return id;
  }

  @Override
  public List<Property> getInfo() {
    return PropertyUtils.createProperties("Id", getId(),
        "Name", getName(),
        "Users", getUsers(),
        "Created", formatMillis(getCreated()),
        "Creator", getCreator(),
        "Registered", formatMillis(getRegistered()),
        "Last Access", formatMillis(getLastAccess()),
        "Messages", getMessages().isEmpty() ? null : BeeUtils.bracket(getMessages().size()),
        "Message Count", getMessageCount(),
        "Unread Count", getUnreadCount(),
        "Max Time", formatMillis(getMaxTime()));
  }

  public long getLastAccess() {
    return lastAccess;
  }

  public ChatItem getLastMessage() {
    return lastMessage;
  }

  public long getMaxTime() {
    return (getLastMessage() == null) ? BeeConst.LONG_UNDEF : getLastMessage().getTime();
  }

  public int getMessageCount() {
    return messageCount;
  }

  public Collection<ChatItem> getMessages() {
    return messages;
  }

  public String getName() {
    return name;
  }

  public long getRegistered() {
    return registered;
  }

  public int getUnreadCount() {
    return unreadCount;
  }

  public List<Long> getUsers() {
    return users;
  }

  public boolean hasUser(Long userId) {
    return userId != null && getUsers().contains(userId);
  }

  public void incrementMessageCount() {
    setMessageCount(getMessageCount() + 1);
  }

  public boolean invite(Long userId) {
    if (DataUtils.isId(userId) && !getUsers().contains(userId)) {
      getUsers().add(userId);
      return true;
    } else {
      return false;
    }
  }

  public boolean is(Long roomId) {
    return Objects.equals(roomId, getId());
  }

  public boolean isOwner(Long userId) {
    return userId != null && userId.equals(getCreator());
  }

  public boolean join(Long userId) {
    if (DataUtils.isId(userId) && !getUsers().contains(userId)) {
      getUsers().add(userId);
      return true;

    } else {
      return false;
    }
  }

  public boolean kick(Long userId) {
    return getUsers().remove(userId);
  }

  public boolean quit(Long userId) {
    return getUsers().remove(userId);
  }

  @Override
  public String serialize() {
    Object[] arr = new Object[Serial.values().length];
    int i = 0;

    for (Serial member : Serial.values()) {
      switch (member) {
        case ID:
          arr[i++] = getId();
          break;

        case NAME:
          arr[i++] = getName();
          break;

        case USERS:
          arr[i++] = getUsers();
          break;

        case CREATED:
          arr[i++] = getCreated();
          break;

        case CREATOR:
          arr[i++] = getCreator();
          break;

        case REGISTERED:
          arr[i++] = getRegistered();
          break;

        case LAST_ACCESS:
          arr[i++] = getLastAccess();
          break;

        case MESSAGES:
          arr[i++] = getMessages();
          break;

        case MESSAGE_COUNT:
          arr[i++] = getMessageCount();
          break;

        case UNREAD_COUNT:
          arr[i++] = getUnreadCount();
          break;

        case LAST_MESSAGE:
          arr[i++] = getLastMessage();
          break;
      }
    }
    return Codec.beeSerialize(arr);
  }

  public void setCreated(long created) {
    this.created = created;
  }

  public void setCreator(long creator) {
    this.creator = creator;
  }

  public void setLastAccess(long lastAccess) {
    this.lastAccess = lastAccess;
  }

  public void setLastMessage(ChatItem lastMessage) {
    this.lastMessage = lastMessage;
  }

  public void setMessageCount(int messageCount) {
    this.messageCount = messageCount;
  }

  public void setName(String name) {
    this.name = name;
  }

  public void setRegistered(long registered) {
    this.registered = registered;
  }

  public void setUnreadCount(int unreadCount) {
    this.unreadCount = unreadCount;
  }

  @Override
  public String toString() {
    return BeeUtils.joinOptions("id", getId(),
        "name", getName(),
        "users", getUsers(),
        "messages", BeeUtils.size(getMessages()),
        "message count", getMessageCount(),
        "unread count", getUnreadCount(),
        "max time", formatMillis(getMaxTime()));
  }

  private void setId(long id) {
    this.id = id;
  }
}
