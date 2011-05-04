package com.butent.bee.client.event;

/**
 * Requires implementing classes to have start of a drag and drop handling method.
 */

public interface HasDragStartHandler {
  boolean onDragStart(DndEvent event);
}
