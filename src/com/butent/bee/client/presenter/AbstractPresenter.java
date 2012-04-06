package com.butent.bee.client.presenter;

import com.google.gwt.user.client.ui.Widget;

import com.butent.bee.shared.ui.Action;

public abstract class AbstractPresenter implements Presenter {

  private String eventSource = null;
  
  public String getEventSource() {
    return eventSource;
  }

  public Widget getWidget() {
    return null;
  }

  public abstract void handleAction(Action action);

  public void onViewUnload() {
  }

  public void setEventSource(String eventSource) {
    this.eventSource = eventSource;
  }
}
