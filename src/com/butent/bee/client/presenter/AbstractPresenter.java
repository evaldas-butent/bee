package com.butent.bee.client.presenter;

import com.google.gwt.user.client.ui.Widget;

import com.butent.bee.shared.ui.Action;

public abstract class AbstractPresenter implements Presenter {

  public Widget getWidget() {
    return null;
  }

  public abstract void handleAction(Action action);

  public void onViewUnload() {
  }
}
