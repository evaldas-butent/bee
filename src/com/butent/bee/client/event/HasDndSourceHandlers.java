package com.butent.bee.client.event;

/**
 * requires that drag and drop source handlers would have events for start, drag and end stages.
 */

public interface HasDndSourceHandlers extends HasDragStartHandler, HasDragHandler,
    HasDragEndHandler {
}
