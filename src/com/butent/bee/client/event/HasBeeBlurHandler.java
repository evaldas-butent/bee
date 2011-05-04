package com.butent.bee.client.event;

import com.google.gwt.event.dom.client.BlurEvent;

/**
 * Requires implementing classes to have a method to handle situations when components loose focus.
 */

public interface HasBeeBlurHandler {
  boolean onBeeBlur(BlurEvent event);

}
