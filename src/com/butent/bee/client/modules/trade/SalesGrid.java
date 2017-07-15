package com.butent.bee.client.modules.trade;

import com.butent.bee.client.view.grid.interceptor.GridInterceptor;

public class SalesGrid extends InvoicesGrid {

  @Override
  public GridInterceptor getInstance() {

    return new SalesGrid();
  }
}
