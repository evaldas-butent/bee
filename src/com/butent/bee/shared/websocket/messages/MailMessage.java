package com.butent.bee.shared.websocket.messages;

import com.butent.bee.shared.utils.Codec;

public class MailMessage extends Message {

  private boolean newMail;

  public MailMessage(boolean newMail) {
    this();
    this.newMail = newMail;
  }

  MailMessage() {
    super(Type.MAIL);
  }

  @Override
  public String brief() {
    return toString();
  }

  public boolean isNewMail() {
    return newMail;
  }

  @Override
  public boolean isValid() {
    return true;
  }

  @Override
  public String toString() {
    return newMail ? "New mail" : "";
  }

  @Override
  protected void deserialize(String s) {
    newMail = Codec.unpack(s);
  }

  @Override
  protected String serialize() {
    return Codec.pack(newMail);
  }

}
