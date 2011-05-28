package com.butent.bee.client.view.edit;

import com.google.gwt.event.shared.HandlerRegistration;

public interface HasEditEndHandlers {

  HandlerRegistration addEditEndHandler(EditEndEvent.Handler handler);
}
