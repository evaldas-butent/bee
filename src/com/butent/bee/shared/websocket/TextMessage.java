package com.butent.bee.shared.websocket;

public class TextMessage extends Message {
  
  private String text;

  public TextMessage(String text) {
    this();
    this.text = text;
  }

  TextMessage() {
    super(Type.TEXT);
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
