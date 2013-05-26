package com.butent.bee.client.view.search;

import com.google.common.collect.Lists;
import com.google.gwt.core.client.Scheduler;
import com.google.gwt.dom.client.Element;

import com.butent.bee.shared.data.BeeColumn;
import com.butent.bee.shared.data.filter.Filter;

import java.util.List;

public class StarFilterSupplier extends AbstractFilterSupplier {

  public StarFilterSupplier(String viewName, BeeColumn column, String options) {
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
  public void onRequest(Element target, Scheduler.ScheduledCommand onChange) {
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
