package com.butent.bee.client.view.edit;

import com.google.gwt.event.shared.HandlerRegistration;

/**
 * Requires implementing classes to have a handler for edit end.
 */

public interface HasEditEndHandlers {

  HandlerRegistration addEditEndHandler(EditEndEvent.Handler handler);
}
