package com.butent.bee.shared.websocket.messages;

import com.google.common.collect.Lists;

import com.butent.bee.shared.Assert;
import com.butent.bee.shared.utils.BeeUtils;
import com.butent.bee.shared.utils.Codec;

import java.util.List;

public class NotificationMessage extends Message implements HasRecipient {

  public static final int MAX_LENGTH = 280;

  private String from;
  private String to;

  private String text;

  public NotificationMessage(String from, String to, String text) {
    this();

    this.from = from;
    this.to = to;
    this.text = text;
  }

  NotificationMessage() {
    super(Type.NOTIFICATION);
  }

  public String getFrom() {
    return from;
  }

  public String getText() {
    return text;
  }

  @Override
  public String getTo() {
    return to;
  }

  public boolean isValid() {
    return !BeeUtils.anyEmpty(getFrom(), getTo(), getText());
  }

  @Override
  public String toString() {
    return BeeUtils.joinOptions("type", string(getType()), "from", getFrom(), "to", getTo(),
        "text", getText());
  }

  @Override
  protected void deserialize(String s) {
    String[] arr = Codec.beeDeserializeCollection(s);
    Assert.lengthEquals(arr, 3);

    int i = 0;
    setFrom(arr[i++]);
    setTo(arr[i++]);
    setText(arr[i++]);
  }

  @Override
  protected String serialize() {
    List<String> values = Lists.newArrayList(getFrom(), getTo(), getText());
    return Codec.beeSerialize(values);
  }

  private void setFrom(String from) {
    this.from = from;
  }

  private void setText(String text) {
    this.text = text;
  }

  private void setTo(String to) {
    this.to = to;
  }
}
