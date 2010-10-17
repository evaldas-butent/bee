package com.butent.bee.egg.client.pst;

import com.google.gwt.event.shared.HandlerRegistration;
import com.google.gwt.event.shared.HasHandlers;

public interface HasRowCountChangeHandlers extends HasHandlers {
  HandlerRegistration addRowCountChangeHandler(RowCountChangeHandler handler);
}
