package com.butent.bee.client.view.edit;

import com.google.gwt.event.shared.HandlerRegistration;

/**
 * Requires implementing classes to have a handler for ready for update event.
 */

public interface HasReadyForUpdateHandlers {

  HandlerRegistration addReadyForUpdateHandler(ReadyForUpdateEvent.Handler handler);
}
