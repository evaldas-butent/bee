package com.butent.bee.client.view.add;

import com.google.gwt.event.shared.HandlerRegistration;

/**
 * Requires for implementing classes to have a add start handler registration method.
 */

public interface HasAddStartHandlers {

  HandlerRegistration addAddStartHandler(AddStartEvent.Handler handler);
}
