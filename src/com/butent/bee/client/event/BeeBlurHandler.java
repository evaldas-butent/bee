package com.butent.bee.client.event;

import com.google.gwt.event.dom.client.BlurEvent;
import com.google.gwt.event.dom.client.BlurHandler;

public class BeeBlurHandler implements BlurHandler {

  public void onBlur(BlurEvent event) {
    Object source = event.getSource();

    if (source instanceof HasBeeBlurHandler) {
      ((HasBeeBlurHandler) source).onBeeBlur(event);
    }
  }

}
