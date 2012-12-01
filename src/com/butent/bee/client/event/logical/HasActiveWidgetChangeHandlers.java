package com.butent.bee.client.event.logical;

import com.google.gwt.event.shared.HandlerRegistration;
import com.google.gwt.event.shared.HasHandlers;

public interface HasActiveWidgetChangeHandlers extends HasHandlers {
  HandlerRegistration addActiveWidgetChangeHandler(ActiveWidgetChangeEvent.Handler handler);
}
