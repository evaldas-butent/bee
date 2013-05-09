package com.butent.bee.client.view.search;

import com.google.common.collect.Lists;
import com.google.gwt.dom.client.Element;

import com.butent.bee.client.Callback;
import com.butent.bee.shared.NotificationListener;
import com.butent.bee.shared.data.BeeColumn;
import com.butent.bee.shared.data.filter.Filter;

import java.util.List;

public class ComparisonFilterSupplier extends AbstractFilterSupplier {

  public ComparisonFilterSupplier(String viewName, BeeColumn column, String options) {
    super(viewName, column, options);
  }

  @Override
  protected List<SupplierAction> getActions() {
    return Lists.newArrayList();
  }
  
  @Override
  public String getDisplayHtml() {
    return null;
  }

  @Override
  public void onRequest(Element target, NotificationListener notificationListener, 
      Callback<Boolean> callback) {
  }

  @Override
  public Filter parse(String value) {
    return null;
  }
}
