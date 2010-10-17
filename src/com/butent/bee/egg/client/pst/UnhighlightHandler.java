package com.butent.bee.egg.client.pst;

import com.google.gwt.event.shared.EventHandler;

public interface UnhighlightHandler<V> extends EventHandler {
  void onUnhighlight(UnhighlightEvent<V> event);
}
