package com.butent.bee.client.presenter;

import com.google.gwt.user.client.ui.Widget;

import com.butent.bee.client.view.View;

public interface Presenter {
  Widget getWidget();
  
  void onUnload(View view);
}
