package com.butent.bee.client.view.edit;

import com.google.gwt.event.shared.HandlerRegistration;

/**
 * Requires implementing classes to have a handler for edit stop.
 */

public interface HasEditStopHandlers {

  HandlerRegistration addEditStopHandler(EditStopEvent.Handler handler);
}
