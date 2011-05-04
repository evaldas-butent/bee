package com.butent.bee.client.event;

/**
 * Requires implementing classes to have a method to handle a situation when drag and drop enters
 * area over a particular component.
 */
public interface HasDragEnterHandler {
  boolean onDragEnter(DndEvent event);
}
