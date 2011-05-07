package com.butent.bee.client.view.event;

import com.google.gwt.event.shared.HandlerRegistration;
import com.google.gwt.event.shared.HasHandlers;

import com.butent.bee.shared.data.view.Order;

/**
 * Requires for implementing classes to have a method for getting sort order.
 */

public interface HasSortHandlers extends HasHandlers {
  HandlerRegistration addSortHandler(SortEvent.Handler handler);

  Order getSortOrder();
}
