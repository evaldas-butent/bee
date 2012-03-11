package com.butent.bee.shared.data.event;

import com.google.gwt.event.shared.HandlerRegistration;

public interface HasActiveWidgetChangeHandlers {
  HandlerRegistration addActiveWidgetChangeHandler(ActiveWidgetChangeEvent.Handler handler);
}
