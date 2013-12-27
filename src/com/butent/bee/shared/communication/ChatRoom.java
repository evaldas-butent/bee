package com.butent.bee.shared.communication;

import com.google.common.collect.Lists;

import com.butent.bee.shared.Assert;
import com.butent.bee.shared.BeeSerializable;
import com.butent.bee.shared.data.DataUtils;
import com.butent.bee.shared.utils.BeeUtils;
import com.butent.bee.shared.utils.Codec;

import java.util.Collection;
import java.util.Vector;

public class ChatRoom implements BeeSerializable {

  public enum Type {
    PUBLIC, PRIVATE
  }

  private enum Serial {
    ID, NAME, TYPE, OWNERS, USERS, MESSAGES
  }

  public static final int DEFAULT_CAPACITY = 1000;

  private static long idCounter;

  public static ChatRoom restore(String s) {
    ChatRoom room = new ChatRoom();
    room.deserialize(s);
    return room;
  }

  private long id;
  private String name;

  private Type type;

  private final Vector<Long> owners = new Vector<>();
  private final Vector<Long> users = new Vector<>();

  private final Collection<TextMessage> messages;

  public ChatRoom(String name, Type type, Collection<TextMessage> messages) {
    this.id = idCounter++;
    this.name = name;
    this.type = type;
    this.messages = messages;
  }

  private ChatRoom() {
    this.messages = Lists.newArrayList();
  }

  public void addMessage(TextMessage message) {
    if (message != null && message.isValid()) {
      messages.add(message);
    }
  }

  public void addOwner(Long ownerId) {
    if (DataUtils.isId(ownerId) && !getOwners().contains(ownerId)) {
      getOwners().add(ownerId);
    }
  }

  public void addUser(Long userId) {
    if (DataUtils.isId(userId) && !getUsers().contains(userId)) {
      getUsers().add(userId);
    }
  }

  public ChatRoom copyWithoutMessages() {
    ChatRoom copy = new ChatRoom();

    copy.setId(getId());
    copy.setName(getName());

    copy.setType(getType());

    copy.getOwners().addAll(getOwners());
    copy.getUsers().addAll(getUsers());

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

        case TYPE:
          setType(Codec.unpack(Type.class, value));
          break;

        case OWNERS:
          if (!owners.isEmpty()) {
            owners.clear();
          }

          String[] oArr = Codec.beeDeserializeCollection(value);
          if (oArr != null) {
            for (String os : oArr) {
              Long owner = BeeUtils.toLongOrNull(os);
              if (DataUtils.isId(owner)) {
                owners.add(owner);
              }
            }
          }
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
      }
    }
  }

  public long getId() {
    return id;
  }

  public Collection<TextMessage> getMessages() {
    return messages;
  }

  public String getName() {
    return name;
  }

  public Vector<Long> getOwners() {
    return owners;
  }

  public Type getType() {
    return type;
  }

  public Vector<Long> getUsers() {
    return users;
  }

  public boolean isOwner(Long userId) {
    return userId != null && getOwners().contains(userId);
  }
  
  public boolean isVisible(Long userId) {
    if (userId == null) {
      return false;
    } else if (getType() == Type.PUBLIC) {
      return true;
    } else {
      return getOwners().contains(userId) || getUsers().contains(userId);
    }
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

        case TYPE:
          arr[i++] = Codec.pack(getType());
          break;

        case OWNERS:
          arr[i++] = getOwners();
          break;

        case USERS:
          arr[i++] = getUsers();
          break;

        case MESSAGES:
          arr[i++] = getMessages();
          break;
      }
    }
    return Codec.beeSerialize(arr);
  }

  public void setName(String name) {
    this.name = name;
  }

  public void setType(Type type) {
    this.type = type;
  }

  @Override
  public String toString() {
    return BeeUtils.joinOptions("id", BeeUtils.toString(getId()),
        "name", getName(),
        "type", (getType() == null) ? null : getType().name().toLowerCase(),
        "owners", getOwners().isEmpty() ? null : owners.toString(),
        "users", getUsers().isEmpty() ? null : users.toString(),
        "messages", (getMessages() == null) ? null : BeeUtils.toString(getMessages().size()));
  }

  private void setId(long id) {
    this.id = id;
  }
}
