package com.butent.bee.client.event;

/**
 * Requires implementing classes to have a method to handle an event after user interface component
 * changes it's parameters.
 */

public interface HasBeeChangeHandler {
  boolean onChange();

}
