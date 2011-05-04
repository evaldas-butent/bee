package com.butent.bee.client.event;

/**
 * Requires that implementing classes would have a method which would handle during drag event.
 */

public interface HasDragHandler {
  boolean onDrag(DndEvent event);
}
