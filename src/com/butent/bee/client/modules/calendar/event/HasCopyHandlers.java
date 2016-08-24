package com.butent.bee.client.modules.calendar.event;

import com.google.gwt.event.shared.HandlerRegistration;
import com.google.gwt.event.shared.HasHandlers;

public interface HasCopyHandlers extends HasHandlers {
  HandlerRegistration addCopyHandler(CopyEvent.Handler handler);
}
