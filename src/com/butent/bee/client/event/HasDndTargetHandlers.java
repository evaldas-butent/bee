package com.butent.bee.client.event;

/**
 * Requires implementing classes to have methods for drag and drop enter, over, leave and drop event
 * handling.
 */

public interface HasDndTargetHandlers extends HasDropHandler, HasDragEnterHandler,
    HasDragLeaveHandler, HasDragOverHandler {
}
