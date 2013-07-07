package com.butent.bee.shared.i18n;

public final class Localized {

  private static LocalizableConstants constants;
  private static LocalizableMessages messages;

  public static LocalizableConstants getConstants() {
    return constants;
  }

  public static LocalizableMessages getMessages() {
    return messages;
  }

  public static void setConstants(LocalizableConstants constants) {
    Localized.constants = constants;
  }

  public static void setMessages(LocalizableMessages messages) {
    Localized.messages = messages;
  }

  private Localized() {
  }
}
