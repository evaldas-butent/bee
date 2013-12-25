package com.butent.bee.shared.websocket.messages;

import com.butent.bee.shared.utils.BeeUtils;

public class EchoMessage extends Message {
  
  private String text;

  public EchoMessage(String text) {
    this();
    this.text = text;
  }

  EchoMessage() {
    super(Type.ECHO);
  }

  public String getText() {
    return text;
  }

  @Override
  public String toString() {
    return BeeUtils.joinOptions("type", string(getType()), "text", getText());
  }
  
  @Override
  protected void deserialize(String s) {
    this.text = s;
  }

  @Override
  protected String serialize() {
    return text;
  }
}
