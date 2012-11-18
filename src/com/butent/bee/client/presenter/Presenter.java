package com.butent.bee.client.presenter;

import com.butent.bee.client.ui.IdentifiableWidget;
import com.butent.bee.client.view.HeaderView;
import com.butent.bee.client.view.View;
import com.butent.bee.shared.ui.HandlesActions;
import com.butent.bee.shared.ui.HasCaption;

public interface Presenter extends HandlesActions, HasCaption {
  
  String getEventSource();

  HeaderView getHeader();
  
  View getMainView();
  
  IdentifiableWidget getWidget();
  
  void onViewUnload();
  
  void setEventSource(String eventSource);
}
