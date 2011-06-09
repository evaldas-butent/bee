package com.butent.bee.shared.data.event;

/**
 * Describes requirements for classes which handle data deletion events.
 */

public interface HandlesDeleteEvents extends RowDeleteEvent.Handler, MultiDeleteEvent.Handler {
}
