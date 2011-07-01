package com.butent.bee.client.view.add;

import com.google.gwt.event.shared.HandlerRegistration;

/**
 * Requires for implementing classes to have a handler registration method for add ending event.
 */

public interface HasAddEndHandlers {

  HandlerRegistration addAddEndHandler(AddEndEvent.Handler handler);
}
