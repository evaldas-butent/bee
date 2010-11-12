package com.butent.bee.egg.client.grid.event;

import com.google.gwt.event.shared.EventHandler;

public interface UnhighlightHandler<V> extends EventHandler {
  void onUnhighlight(UnhighlightEvent<V> event);
}
