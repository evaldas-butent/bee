package com.butent.bee.client.view.search;

import com.butent.bee.shared.data.BeeColumn;

public class DateFilterSupplier extends DateTimeFilterSupplier {

  public DateFilterSupplier(String viewName, BeeColumn column, String label, String options) {
    super(viewName, column, label, options);
  }

  @Override
  protected boolean isDateTime() {
    return false;
  }
}
