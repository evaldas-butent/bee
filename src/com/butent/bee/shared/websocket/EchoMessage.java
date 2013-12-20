package com.butent.bee.shared.websocket;

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
  protected void deserialize(String s) {
    this.text = s;
  }

  @Override
  protected String serialize() {
    return text;
  }
}
