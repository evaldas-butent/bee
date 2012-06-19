package com.butent.bee.client.presenter;

import com.google.gwt.user.client.ui.Widget;

import com.butent.bee.shared.ui.HandlesActions;

public interface Presenter extends HandlesActions {
  
  String getEventSource();

  Widget getWidget();
  
  void onViewUnload();
  
  void setEventSource(String eventSource);
}
