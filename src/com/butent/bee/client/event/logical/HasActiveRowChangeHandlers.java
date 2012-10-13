package com.butent.bee.client.event.logical;

import com.google.gwt.event.shared.HandlerRegistration;

/**
 * Describes requirements for classes which handle active row change events in table based user
 * interface components.
 */

public interface HasActiveRowChangeHandlers {
  HandlerRegistration addActiveRowChangeHandler(ActiveRowChangeEvent.Handler handler);
}
