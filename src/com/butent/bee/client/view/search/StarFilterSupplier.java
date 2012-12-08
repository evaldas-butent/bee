package com.butent.bee.client.view.search;

import com.google.gwt.dom.client.Element;

import com.butent.bee.client.Callback;
import com.butent.bee.client.dialog.NotificationListener;
import com.butent.bee.shared.data.BeeColumn;
import com.butent.bee.shared.data.filter.Filter;

public class StarFilterSupplier extends AbstractFilterSupplier {

  public StarFilterSupplier(String viewName, Filter immutableFilter, BeeColumn column,
      String options) {
    super(viewName, immutableFilter, column, options);
  }

  @Override
  public String getDisplayHtml() {
    return null;
  }
  
  @Override
  public void onRequest(Element target, NotificationListener notificationListener,
      Callback<Boolean> callback) {
  }
}
