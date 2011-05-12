package com.butent.bee.client.presenter;

import com.google.gwt.user.client.ui.Widget;

/**
 * Requires implementing classes to have events for widget getting and view unloading.
 */

public interface Presenter {

  Widget getWidget();
  
  void handleAction(Action action);

  void onViewUnload();
}
