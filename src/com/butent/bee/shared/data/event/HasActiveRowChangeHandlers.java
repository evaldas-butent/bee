package com.butent.bee.shared.data.event;

import com.google.gwt.event.shared.HandlerRegistration;

public interface HasActiveRowChangeHandlers {
  HandlerRegistration addActiveRowChangeHandler(ActiveRowChangeEvent.Handler handler);
}
