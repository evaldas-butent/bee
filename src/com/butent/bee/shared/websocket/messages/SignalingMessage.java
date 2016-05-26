package com.butent.bee.shared.websocket.messages;

import com.google.common.collect.Lists;

import com.butent.bee.shared.Assert;
import com.butent.bee.shared.utils.BeeUtils;
import com.butent.bee.shared.utils.Codec;

import java.util.List;

public class SignalingMessage extends Message implements HasRecipient {

  public static SignalingMessage signal(String from, String to,
      String label, String key, String value) {

    return new SignalingMessage(from, to, label, key, value);
  }

  private String from;
  private String to;

  private String label;
  private String key;
  private String value;

  SignalingMessage() {
    super(Type.SIGNALING);
  }

  private SignalingMessage(String from, String to, String label, String key, String value) {
    this();

    this.from = from;
    this.to = to;

    this.label = label;
    this.key = key;
    this.value = value;
  }

  @Override
  public String brief() {
    return BeeUtils.joinWords(getLabel(), getKey());
  }

  public String getFrom() {
    return from;
  }

  @Override
  public String getTo() {
    return to;
  }

  public String getLabel() {
    return label;
  }

  public String getKey() {
    return key;
  }

  public String getValue() {
    return value;
  }

  @Override
  public boolean isValid() {
    return !BeeUtils.anyEmpty(getFrom(), getTo(), getLabel(), getKey());
  }

  @Override
  public String toString() {
    return BeeUtils.joinOptions("type", string(getType()), "from", getFrom(), "to", getTo(),
        "label", getLabel(), "key", getKey(), "value", getValue());
  }

  @Override
  protected void deserialize(String s) {
    String[] arr = Codec.beeDeserializeCollection(s);
    Assert.lengthEquals(arr, 5);

    int i = 0;
    setFrom(arr[i++]);
    setTo(arr[i++]);
    setLabel(arr[i++]);
    setKey(arr[i++]);
    setValue(arr[i++]);
  }

  @Override
  protected String serialize() {
    List<String> values = Lists.newArrayList(getFrom(), getTo(), getLabel(), getKey(), getValue());
    return Codec.beeSerialize(values);
  }

  private void setFrom(String from) {
    this.from = from;
  }

  private void setTo(String to) {
    this.to = to;
  }

  private void setLabel(String label) {
    this.label = label;
  }

  private void setKey(String key) {
    this.key = key;
  }

  private void setValue(String value) {
    this.value = value;
  }
}
