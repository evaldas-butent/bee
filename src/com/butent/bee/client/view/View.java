package com.butent.bee.client.view;

import com.google.gwt.user.client.ui.IsWidget;

import com.butent.bee.client.presenter.Presenter;

public interface View extends IsWidget {
  Presenter getViewPresenter();
  void setViewPresenter(Presenter viewPresenter);
}
