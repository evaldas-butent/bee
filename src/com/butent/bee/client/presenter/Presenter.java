package com.butent.bee.client.presenter;

import com.google.gwt.user.client.ui.Widget;

import com.butent.bee.shared.ui.Action;

/**
 * Requires implementing classes to have events for widget getting and view unloading.
 */

public interface Presenter {
  
  String getEventSource();

  Widget getWidget();
  
  void handleAction(Action action);

  void onViewUnload();
  
  void setEventSource(String eventSource);
}
