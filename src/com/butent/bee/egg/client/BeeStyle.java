package com.butent.bee.egg.client;

public class BeeStyle implements BeeModule {
  public static final String RADIO_BUTTON_SELECTED = "bee-selectedRadio";
  public static final String ACTIVE_BLANK = "bee-activeBlank";
  public static final String ACTIVE_CONTENT = "bee-activeContent";

  public void end() {
  }

  public String getName() {
    return getClass().getName();
  }

  public int getPriority(int p) {
    switch (p) {
      case PRIORITY_INIT:
        return DO_NOT_CALL;
      case PRIORITY_START:
        return DO_NOT_CALL;
      case PRIORITY_END:
        return DO_NOT_CALL;
      default:
        return DO_NOT_CALL;
    }
  }

  public void init() {
  }

  public void start() {
  }

}
