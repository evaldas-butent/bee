package com.butent.bee.shared.communication;

import com.butent.bee.shared.Assert;
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
import java.util.Vector;

public class ChatRoom implements BeeSerializable, HasInfo {

  private enum Serial {
    ID, NAME, USERS, MESSAGES, MESSAGE_COUNT, MAX_TIME
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

  private final Vector<Long> users = new Vector<>();

  private Long creator;

  private final Collection<TextMessage> messages;

  private int messageCount;
  private long maxTime;

  public ChatRoom(long id, String name, Collection<TextMessage> messages) {
    this.id = id;
    this.name = name;
    this.messages = messages;
  }

  private ChatRoom() {
    this.messages = new ArrayList<>();
  }

  public void clear() {
    messages.clear();

    setMessageCount(0);
    setMaxTime(0);
  }

  public ChatRoom copyWithoutMessages() {
    ChatRoom copy = new ChatRoom();

    copy.setId(getId());
    copy.setName(getName());

    if (!BeeUtils.isEmpty(getUsers())) {
      copy.getUsers().addAll(getUsers());
    }

    copy.setMessageCount(getMessageCount());
    copy.setMaxTime(getMaxTime());

    return copy;
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

        case MESSAGES:
          if (!messages.isEmpty()) {
            messages.clear();
          }

          String[] mArr = Codec.beeDeserializeCollection(value);
          if (mArr != null) {
            for (String msg : mArr) {
              messages.add(TextMessage.restore(msg));
            }
          }
          break;

        case MESSAGE_COUNT:
          setMessageCount(BeeUtils.toInt(value));
          break;

        case MAX_TIME:
          setMaxTime(BeeUtils.toLong(value));
          break;
      }
    }
  }

  public long getId() {
    return id;
  }

  @Override
  public List<Property> getInfo() {
    return PropertyUtils.createProperties("Room Id", getId(),
        "Name", getName(),
        "Users", getUsers(),
        "Messages", (getMessages() == null) ? null : BeeUtils.bracket(getMessages().size()),
        "Message Count", getMessageCount(),
        "Max Time", formatMillis(getMaxTime()));
  }

  public Long getCreator() {
    return creator;
  }

  public long getMaxTime() {
    return maxTime;
  }

  public int getMessageCount() {
    return messageCount;
  }

  public Collection<TextMessage> getMessages() {
    return messages;
  }

  public String getName() {
    return name;
  }

  public Vector<Long> getUsers() {
    return users;
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

  public boolean isVisible(Long userId) {
    if (userId == null) {
      return false;
    } else {
      return getUsers().contains(userId);
    }
  }

  public boolean join(Long userId) {
    if (isVisible(userId)) {
      if (!getUsers().contains(userId)) {
        getUsers().add(userId);
      }
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

        case MESSAGES:
          arr[i++] = getMessages();
          break;

        case MESSAGE_COUNT:
          arr[i++] = getMessageCount();
          break;

        case MAX_TIME:
          arr[i++] = getMaxTime();
          break;
      }
    }
    return Codec.beeSerialize(arr);
  }

  public void setCreator(Long creator) {
    this.creator = creator;
  }

  public void setMaxTime(long maxTime) {
    this.maxTime = maxTime;
  }

  public void setMessageCount(int messageCount) {
    this.messageCount = messageCount;
  }

  public void setName(String name) {
    this.name = name;
  }

  @Override
  public String toString() {
    return BeeUtils.joinOptions("id", BeeUtils.toString(getId()),
        "name", getName(),
        "users", getUsers().isEmpty() ? null : getUsers().toString(),
        "messages", (getMessages() == null) ? null : BeeUtils.toString(getMessages().size()),
        "message count", BeeUtils.toString(getMessageCount()),
        "max time", formatMillis(getMaxTime()));
  }

  public void updateMaxTime(long time) {
    if (time > 0) {
      setMaxTime(Math.max(getMaxTime(), time));
    }
  }

  private void setId(long id) {
    this.id = id;
  }
}
