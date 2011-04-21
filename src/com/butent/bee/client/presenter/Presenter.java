package com.butent.bee.client.presenter;

import com.google.gwt.user.client.ui.Widget;

public interface Presenter {
  Widget getWidget();
  
  void onViewUnload();
}
