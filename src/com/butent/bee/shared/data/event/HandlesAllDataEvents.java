package com.butent.bee.shared.data.event;

/**
 * Determines that implementators would be able to handle insert, update and delete events.
 */

public interface HandlesAllDataEvents extends HandlesDeleteEvents, HandlesUpdateEvents,
    RowInsertEvent.Handler, DataChangeEvent.Handler {
}
