package com.butent.bee.client.presenter;

import com.google.gwt.user.client.ui.Widget;

import com.butent.bee.shared.ui.HandlesActions;
import com.butent.bee.shared.ui.HasCaption;

public interface Presenter extends HandlesActions, HasCaption {
  
  String getEventSource();

  Widget getWidget();
  
  void onViewUnload();
  
  void setEventSource(String eventSource);
}
