package com.butent.bee.shared.communication;

import com.google.common.collect.Lists;

import com.butent.bee.shared.Assert;
import com.butent.bee.shared.BeeSerializable;
import com.butent.bee.shared.data.DataUtils;
import com.butent.bee.shared.time.TimeUtils;
import com.butent.bee.shared.utils.BeeUtils;
import com.butent.bee.shared.utils.Codec;

import java.util.List;

public class ChatItem implements BeeSerializable, Comparable<ChatItem> {

  public static ChatItem restore(String s) {
    ChatItem chatItem = new ChatItem();
    chatItem.deserialize(s);
    return chatItem;
  }

  private long userId;
  private long time;

  private String text;

  public ChatItem(long userId, String text) {
    this(userId, System.currentTimeMillis(), text);
  }

  public ChatItem(long userId, long time, String text) {
    this.userId = userId;
    this.time = time;

    this.text = text;
  }

  private ChatItem() {
  }

  @Override
  public int compareTo(ChatItem o) {
    return Long.compare(time, o.time);
  }

  @Override
  public void deserialize(String s) {
    String[] arr = Codec.beeDeserializeCollection(s);
    Assert.lengthEquals(arr, 3);

    int i = 0;

    setUserId(BeeUtils.toLong(arr[i++]));
    setTime(BeeUtils.toLong(arr[i++]));
    setText(arr[i++]);
  }

  public String getText() {
    return text;
  }

  public long getTime() {
    return time;
  }

  public long getUserId() {
    return userId;
  }

  public boolean isValid() {
    return DataUtils.isId(getUserId()) && getTime() > 0 && !BeeUtils.isEmpty(getText());
  }

  @Override
  public String serialize() {
    List<String> values = Lists.newArrayList(BeeUtils.toString(getUserId()),
        BeeUtils.toString(getTime()), getText());
    return Codec.beeSerialize(values);
  }

  @Override
  public String toString() {
    return BeeUtils.joinOptions("userId", getUserId(),
        "time", (getTime() > 0) ? TimeUtils.renderDateTime(getTime(), true) : null,
        "text", BeeUtils.clip(getText(), 50));
  }

  private void setText(String text) {
    this.text = text;
  }

  private void setTime(long time) {
    this.time = time;
  }

  private void setUserId(long userId) {
    this.userId = userId;
  }
}
