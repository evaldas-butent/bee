package com.butent.bee.client.event.logical;

import com.google.gwt.event.shared.HandlerRegistration;

public interface HasActiveWidgetChangeHandlers {
  HandlerRegistration addActiveWidgetChangeHandler(ActiveWidgetChangeEvent.Handler handler);
}
