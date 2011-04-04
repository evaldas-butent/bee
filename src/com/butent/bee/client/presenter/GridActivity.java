package com.butent.bee.client.presenter;

import com.google.gwt.activity.shared.AbstractActivity;
import com.google.gwt.event.shared.EventBus;
import com.google.gwt.user.client.ui.AcceptsOneWidget;

import com.butent.bee.client.view.Search;

public class GridActivity extends AbstractActivity implements Search.Presenter {

  @Override
  public void start(AcceptsOneWidget panel, EventBus eventBus) {
  }

  @Override
  public void updateFilter(String filter) {
  }

}
