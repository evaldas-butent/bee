package com.butent.bee.shared.data.event;

public interface HandlesAllDataEvents extends HandlesDeleteEvents, HandlesUpdateEvents,
    RowInsertEvent.Handler {
}
