package com.butent.bee.client.view.edit;

import com.google.gwt.event.shared.HandlerRegistration;

public interface HasReadyForUpdateHandlers {

  HandlerRegistration addReadyForUpdateHandler(ReadyForUpdateEvent.Handler handler);
}
