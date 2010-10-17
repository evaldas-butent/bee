package com.butent.bee.egg.client.pst;

import com.google.gwt.event.shared.HandlerRegistration;

public interface HasUnhighlightHandlers<V> {
  HandlerRegistration addUnhighlightHandler(UnhighlightHandler<V> handler);
}
