package com.butent.bee.client.view.search;

import com.google.common.collect.Lists;
import com.google.gwt.dom.client.Element;

import com.butent.bee.client.Callback;
import com.butent.bee.shared.NotificationListener;
import com.butent.bee.shared.data.BeeColumn;
import com.butent.bee.shared.data.filter.Filter;

import java.util.List;

public class FlagFilterSupplier extends AbstractFilterSupplier {

  public FlagFilterSupplier(String viewName, BeeColumn column, String options) {
    super(viewName, column, options);
  }

  @Override
  public String getLabel() {
    return null;
  }
  
  @Override
  public String getValue() {
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
  
  @Override
  public void setValue(String value) {
  }
  
  @Override
  protected List<SupplierAction> getActions() {
    return Lists.newArrayList();
  }
}
