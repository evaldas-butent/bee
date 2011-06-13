package com.butent.bee.shared.data.event;

/**
 * Describes requirements for classes which handle data update events.
 */

public interface HandlesUpdateEvents extends CellUpdateEvent.Handler, RowUpdateEvent.Handler {
}
