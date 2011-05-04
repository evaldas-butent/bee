package com.butent.bee.client.event;

/**
 * Requires implementing classes to have a method for drop event in drag and drop situation.
 */
public interface HasDropHandler {
  boolean onDrop(DndEvent event);
}
