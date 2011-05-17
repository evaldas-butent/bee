package com.butent.bee.shared.data.event;

import com.google.gwt.event.shared.HandlerRegistration;

public interface HasSelectionCountChangeHandlers {
  HandlerRegistration addSelectionCountChangeHandler(SelectionCountChangeEvent.Handler handler);

}
