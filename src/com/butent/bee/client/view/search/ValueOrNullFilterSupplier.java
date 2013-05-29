package com.butent.bee.client.view.search;

import com.butent.bee.shared.data.BeeColumn;

public class ValueOrNullFilterSupplier extends ValueFilterSupplier {

  public ValueOrNullFilterSupplier(String viewName, BeeColumn column, String label, String options) {
    super(viewName, column, label, options);
  }

}
