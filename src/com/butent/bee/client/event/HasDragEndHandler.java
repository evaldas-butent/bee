package com.butent.bee.client.event;

/**
 * Requires implementing classes would have a end of a drag and drop event.
 */

public interface HasDragEndHandler {
  boolean onDragEnd(DndEvent event);
}
