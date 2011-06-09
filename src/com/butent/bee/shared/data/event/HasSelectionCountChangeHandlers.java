package com.butent.bee.shared.data.event;

import com.google.gwt.event.shared.HandlerRegistration;

/**
 * Describes requirements for classes which handle selection count change events.
 */

public interface HasSelectionCountChangeHandlers {
  HandlerRegistration addSelectionCountChangeHandler(SelectionCountChangeEvent.Handler handler);
}
