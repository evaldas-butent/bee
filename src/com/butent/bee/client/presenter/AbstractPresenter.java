package com.butent.bee.client.presenter;

import com.butent.bee.client.ui.IdentifiableWidget;

public abstract class AbstractPresenter implements Presenter {

  private String eventSource = null;
  
  @Override
  public String getEventSource() {
    return eventSource;
  }

  @Override
  public IdentifiableWidget getWidget() {
    return null;
  }

  @Override
  public void onViewUnload() {
  }

  @Override
  public void setEventSource(String eventSource) {
    this.eventSource = eventSource;
  }
}
