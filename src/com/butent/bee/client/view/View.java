package com.butent.bee.client.view;

import com.google.gwt.user.client.ui.HasEnabled;

import com.butent.bee.client.presenter.Presenter;
import com.butent.bee.client.ui.IdentifiableWidget;

/**
 * Extends GWT IsWidget class and requires implementing classes to have view presenter getters and
 * setters and a method to get widget's ID.
 */

public interface View extends IdentifiableWidget, HasEnabled {
  
  Presenter getViewPresenter();

  String getWidgetId();

  void setViewPresenter(Presenter viewPresenter);
}
