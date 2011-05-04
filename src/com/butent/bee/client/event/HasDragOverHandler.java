package com.butent.bee.client.event;

/**
 * Requires implementing classes to have a method to handle a situation when drag and drop moves
 * over area a particular component.
 */

public interface HasDragOverHandler {
  boolean onDragOver(DndEvent event);
}
