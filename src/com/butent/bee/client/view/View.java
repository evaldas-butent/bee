package com.butent.bee.client.view;

import com.google.gwt.user.client.ui.HasEnabled;
import com.google.gwt.user.client.ui.IsWidget;

import com.butent.bee.client.presenter.Presenter;

/**
 * Extends GWT IsWidget class and requires implementing classes to have view presenter getters and
 * setters and a method to get widget's ID.
 */

public interface View extends IsWidget, HasEnabled {
  
  Presenter getViewPresenter();

  String getWidgetId();

  void setViewPresenter(Presenter viewPresenter);
}
