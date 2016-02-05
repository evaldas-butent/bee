package com.butent.bee.shared.websocket.messages;

import com.butent.bee.shared.Assert;
import com.butent.bee.shared.communication.ChatItem;
import com.butent.bee.shared.utils.BeeUtils;
import com.butent.bee.shared.utils.Codec;

import java.util.ArrayList;
import java.util.List;

public class ChatMessage extends Message {

  private long chatId;
  private ChatItem chatItem;

  public ChatMessage(long chatId, ChatItem chatItem) {
    this();

    this.chatId = chatId;
    this.chatItem = chatItem;
  }

  ChatMessage() {
    super(Type.CHAT);
  }

  @Override
  public String brief() {
    return (getChatItem() == null) ? null : getChatItem().getText();
  }

  public long getChatId() {
    return chatId;
  }

  public ChatItem getChatItem() {
    return chatItem;
  }

  @Override
  public boolean isValid() {
    return getChatItem() != null && getChatItem().isValid();
  }

  @Override
  public String toString() {
    return BeeUtils.joinOptions("chatId", getChatId(),
        "message", (getChatItem() == null) ? null : getChatItem().toString());
  }

  @Override
  protected void deserialize(String s) {
    String[] arr = Codec.beeDeserializeCollection(s);
    Assert.lengthEquals(arr, 2);

    setChatId(BeeUtils.toLong(arr[0]));
    setChatItem(ChatItem.restore(arr[1]));
  }

  @Override
  protected String serialize() {
    List<Object> values = new ArrayList<>();

    values.add(getChatId());
    values.add(getChatItem());

    return Codec.beeSerialize(values);
  }

  private void setChatId(long chatId) {
    this.chatId = chatId;
  }

  private void setChatItem(ChatItem chatItem) {
    this.chatItem = chatItem;
  }
}
