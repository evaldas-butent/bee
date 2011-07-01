package com.butent.bee.client.view.add;

import com.google.gwt.event.shared.HandlerRegistration;

/**
 * Requires for implementing classes to have a ready for insert handler registration method.
 */

public interface HasReadyForInsertHandlers {

  HandlerRegistration addReadyForInsertHandler(ReadyForInsertEvent.Handler handler);
}
