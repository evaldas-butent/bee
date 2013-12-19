package com.butent.bee.shared.websocket;

import com.google.common.collect.Lists;

import com.butent.bee.shared.logging.BeeLogger;
import com.butent.bee.shared.logging.LogUtils;
import com.butent.bee.shared.utils.BeeUtils;
import com.butent.bee.shared.utils.Codec;
import com.butent.bee.shared.utils.EnumUtils;

import java.util.List;

public abstract class Message {
  
  public enum Type {
    INFO {
      @Override
      Message createMessage() {
        return new InfoMessage();
      }
    },
    
    TEXT {
      @Override
      Message createMessage() {
        return new TextMessage();
      }
    };
    
    abstract Message createMessage();
  }
  
  private static BeeLogger logger = LogUtils.getLogger(Message.class);
  
  public static Message decode(String s) {
    String[] arr = Codec.beeDeserializeCollection(s);
    if (arr == null || arr.length != 2) {
      logger.severe("cannot decode message", s);
      return null;
    }
    
    Type messageType = EnumUtils.getEnumByIndex(Type.class, BeeUtils.toIntOrNull(arr[0]));
    if (messageType == null) {
      logger.severe("cannot decode message type", arr[0]);
      return null;
    }
    
    Message message = messageType.createMessage();
    message.deserialize(arr[1]);
    
    return message;
  }
  
  private final Type type;

  protected Message(Type type) {
    this.type = type;
  }
  
  public String encode() {
    List<String> data = Lists.newArrayList(Integer.toString(type.ordinal()), serialize());
    return Codec.beeSerialize(data);
  }
  
  public Type getType() {
    return type;
  }

  protected abstract void deserialize(String s);

  protected abstract String serialize();
}
