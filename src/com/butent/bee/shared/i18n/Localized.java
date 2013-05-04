package com.butent.bee.shared.i18n;

public class Localized {

  public static LocalizableConstants constants;
  public static LocalizableMessages messages;

  public static void setConstants(LocalizableConstants constants) {
    Localized.constants = constants;
  }

  public static void setMessages(LocalizableMessages messages) {
    Localized.messages = messages;
  }
}
