package com.butent.bee.server.communication;

import com.butent.bee.shared.communication.ChatRoom;
import com.butent.bee.shared.communication.TextMessage;
import com.butent.bee.shared.logging.BeeLogger;
import com.butent.bee.shared.logging.LogUtils;
import com.butent.bee.shared.utils.BeeUtils;
import com.butent.bee.shared.utils.Property;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Queue;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.ConcurrentLinkedQueue;

public final class Rooms {

  private static BeeLogger logger = LogUtils.getLogger(Rooms.class);

  private static Queue<ChatRoom> chatRooms = new ConcurrentLinkedQueue<>();
  
  static {
    chatRooms.add(new ChatRoom("101", ChatRoom.Type.PUBLIC, emptyTextMessages()));
  }
  
  public static ChatRoom addRoom(ChatRoom source) {
    if (source == null) {
      logger.severe("cannot add room without source");
      return null;
    }
    if (BeeUtils.isEmpty(source.getName())) {
      logger.severe("room name is required");
      return null;
    }
    if (source.getType() == null) {
      logger.severe("room type is required");
      return null;
    }

    ChatRoom room = new ChatRoom(source.getName(), source.getType(), emptyTextMessages());
    
    if (!BeeUtils.isEmpty(source.getOwners())) {
      room.getOwners().addAll(source.getOwners());
    }
    if (!BeeUtils.isEmpty(source.getDwellers())) {
      room.getDwellers().addAll(source.getDwellers());
    }
    
    if (!BeeUtils.isEmpty(source.getUsers())) {
      room.getUsers().addAll(source.getUsers());
    }

    if (!BeeUtils.isEmpty(source.getMessages())) {
      room.getMessages().addAll(source.getMessages());

      room.setMessageCount(source.getMessageCount());
      room.setMaxTime(source.getMaxTime());
    }
    
    return room;
  }

  public static List<Property> getInfo() {
    List<Property> info = new ArrayList<>();
    info.add(new Property("Server Rooms", BeeUtils.bracket(chatRooms.size())));
    
    for (ChatRoom room : chatRooms) {
      info.addAll(room.getInfo());
    }
    return info;
  }
  
  public static ChatRoom getRoom(Long roomId) {
    for (ChatRoom room : chatRooms) {
      if (room.is(roomId)) {
        return room;
      }
    }
    
    logger.warning("room not found:", roomId);
    return null;
  }
  
  public static List<ChatRoom> getRoomDataWithoutMessagess(Long userId) {
    List<ChatRoom> result = new ArrayList<>();
    
    for (ChatRoom room : chatRooms) {
      if (room.isVisible(userId)) {
        result.add(room.copyWithoutMessages());
      }
    }
    
    return result;
  }
  
  public static ChatRoom removeRoom(Long roomId) {
    ChatRoom room = getRoom(roomId);

    if (room != null && chatRooms.remove(room)) {
      return room;
    } else {
      return null;
    }
  }
  
  public static void sanitizeIncomingMessage(TextMessage textMessage) {
    if (textMessage != null) {
      textMessage.setMillis(System.currentTimeMillis());
    }
  }
  
  public static ChatRoom updateRoom(ChatRoom source) {
    if (source == null) {
      logger.severe("cannot update room without source");
      return null;
    }
    if (BeeUtils.isEmpty(source.getName())) {
      logger.severe("room name is required");
      return null;
    }
    if (source.getType() == null) {
      logger.severe("room type is required");
      return null;
    }

    ChatRoom target = getRoom(source.getId());
    if (target == null) {
      return null;
    }
    
    target.setName(source.getName());
    target.setType(source.getType());
    
    BeeUtils.overwrite(target.getOwners(), source.getOwners());
    BeeUtils.overwrite(target.getDwellers(), source.getDwellers());

    return target;
  }
  
  private static Collection<TextMessage> emptyTextMessages() {
    return new ArrayBlockingQueue<>(ChatRoom.DEFAULT_CAPACITY);
  }
  
  private Rooms() {
  }
}
