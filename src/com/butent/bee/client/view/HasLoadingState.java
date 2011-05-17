package com.butent.bee.client.view;

import com.google.gwt.event.shared.HandlerRegistration;
import com.google.gwt.user.cellview.client.LoadingStateChangeEvent;

public interface HasLoadingState {

  HandlerRegistration addLoadingStateChangeHandler(LoadingStateChangeEvent.Handler handler);

  void fireLoadingStateChange(LoadingStateChangeEvent.LoadingState loadingState);
}
