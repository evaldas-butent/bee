package com.butent.bee.client.view.edit;

import com.google.gwt.event.shared.HandlerRegistration;

/**
 * Requires implementing classes to have a handler for edit start.
 */

public interface HasEditStartHandlers {

  HandlerRegistration addEditStartHandler(EditStartEvent.Handler handler);
}
