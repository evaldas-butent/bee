package com.butent.bee.client.event;

/**
 * Requires implementing classes to have a method to handle a situation when drag and drop leaves
 * the area over a particular component.
 */

public interface HasDragLeaveHandler {
  boolean onDragLeave(DndEvent event);
}
