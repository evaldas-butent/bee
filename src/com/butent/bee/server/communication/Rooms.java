package com.butent.bee.server.communication;

import com.google.common.base.Objects;

import com.butent.bee.shared.communication.ChatRoom;
import com.butent.bee.shared.communication.TextMessage;
import com.butent.bee.shared.logging.BeeLogger;
import com.butent.bee.shared.logging.LogUtils;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Queue;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.ConcurrentLinkedQueue;

public final class Rooms {

  private static BeeLogger logger = LogUtils.getLogger(Rooms.class);

  private static Queue<ChatRoom> chatRooms = new ConcurrentLinkedQueue<>();
  
  public static ChatRoom getRoom(long roomId) {
    for (ChatRoom room : chatRooms) {
      if (Objects.equal(room.getId(), roomId)) {
        return room;
      }
    }
    
    logger.warning("room not found:", roomId);
    return null; 
  }
  
  public static List<ChatRoom> getRoomDataWithoudMessagess(Long userId) {
    List<ChatRoom> result = new ArrayList<>();
    
    for (ChatRoom room : chatRooms) {
      if (room.isVisible(userId)) {
        result.add(room.copyWithoutMessages());
      }
    }
    
    return result;
  }
  
  public static void sanitizeIncomingMessage(TextMessage textMessage) {
    if (textMessage != null) {
      textMessage.setMillis(System.currentTimeMillis());
    }
  }
  
  private static Collection<TextMessage> emptyTextMessages() {
    return new ArrayBlockingQueue<>(ChatRoom.DEFAULT_CAPACITY);
  }
  
  static {
    chatRooms.add(new ChatRoom("101", ChatRoom.Type.PUBLIC, emptyTextMessages()));
  }
  
  private Rooms() {
  }
}
