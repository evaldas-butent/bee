package com.butent.bee.client.view;

import com.google.gwt.user.client.ui.HasEnabled;

import com.butent.bee.client.event.logical.ReadyEvent;
import com.butent.bee.client.presenter.Presenter;
import com.butent.bee.client.ui.IdentifiableWidget;

public interface View extends IdentifiableWidget, HasEnabled, ReadyEvent.HasReadyHandlers {

  Presenter getViewPresenter();

  String getWidgetId();

  void setViewPresenter(Presenter viewPresenter);
}
