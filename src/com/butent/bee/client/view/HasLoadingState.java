package com.butent.bee.client.view;

import com.google.gwt.event.shared.HandlerRegistration;
import com.google.gwt.user.cellview.client.LoadingStateChangeEvent;

/**
 * Requires implementing classes to have methods for adding a loading state change handler and
 * firing of state change events.
 */

public interface HasLoadingState {

  HandlerRegistration addLoadingStateChangeHandler(LoadingStateChangeEvent.Handler handler);

  void fireLoadingStateChange(LoadingStateChangeEvent.LoadingState loadingState);
}
