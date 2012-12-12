package com.butent.bee.client.view.add;

import com.google.gwt.event.shared.HandlerRegistration;

public interface HasReadyForInsertHandlers {
  HandlerRegistration addReadyForInsertHandler(ReadyForInsertEvent.Handler handler);
}
