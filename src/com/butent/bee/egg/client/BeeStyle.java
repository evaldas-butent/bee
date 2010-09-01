package com.butent.bee.egg.client;

public class BeeStyle implements BeeModule {
  public static final String RADIO_BUTTON_SELECTED = "selectedRadio";

  @Override
  public String getName() {
    return getClass().getName();
  }

  @Override
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

  @Override
  public void init() {
  }

  @Override
  public void start() {
  }

  @Override
  public void end() {
  }

}
