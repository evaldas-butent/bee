package com.butent.bee.egg.client.grid.event;

import com.google.gwt.event.shared.HandlerRegistration;

public interface HasUnhighlightHandlers<V> {
  HandlerRegistration addUnhighlightHandler(UnhighlightHandler<V> handler);
}
