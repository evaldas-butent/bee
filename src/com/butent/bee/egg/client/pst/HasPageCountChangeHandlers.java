package com.butent.bee.egg.client.pst;

import com.google.gwt.event.shared.HandlerRegistration;
import com.google.gwt.event.shared.HasHandlers;

public interface HasPageCountChangeHandlers extends HasHandlers {
  HandlerRegistration addPageCountChangeHandler(PageCountChangeHandler handler);
}
