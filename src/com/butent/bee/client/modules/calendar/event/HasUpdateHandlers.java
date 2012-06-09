package com.butent.bee.client.modules.calendar.event;

import com.google.gwt.event.shared.HandlerRegistration;
import com.google.gwt.event.shared.HasHandlers;

public interface HasUpdateHandlers extends HasHandlers {
  HandlerRegistration addUpdateHandler(UpdateEvent.Handler handler);
}
