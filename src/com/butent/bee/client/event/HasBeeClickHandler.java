package com.butent.bee.client.event;

import com.google.gwt.event.dom.client.ClickEvent;

/**
 * Requires implementing classes to have a method for click event.
 */

public interface HasBeeClickHandler {
  boolean onBeeClick(ClickEvent event);

}
