package com.butent.bee.client.modules.trade.acts;

import com.butent.bee.client.view.grid.interceptor.AbstractGridInterceptor;
import com.butent.bee.client.view.grid.interceptor.GridInterceptor;

public class TradeActServicesGrid extends AbstractGridInterceptor {

  TradeActServicesGrid() {
  }

  @Override
  public GridInterceptor getInstance() {
    return new TradeActServicesGrid();
  }
}
