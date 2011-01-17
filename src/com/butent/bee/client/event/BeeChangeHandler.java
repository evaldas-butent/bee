package com.butent.bee.client.event;

import com.google.gwt.event.dom.client.ChangeEvent;
import com.google.gwt.event.dom.client.ChangeHandler;

public class BeeChangeHandler implements ChangeHandler {

  public void onChange(ChangeEvent event) {
    Object source = event.getSource();

    if (source instanceof HasBeeChangeHandler) {
      ((HasBeeChangeHandler) source).onChange();
    }
  }
}
