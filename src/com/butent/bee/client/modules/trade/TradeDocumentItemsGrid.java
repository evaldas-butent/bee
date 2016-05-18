package com.butent.bee.client.modules.trade;

import com.butent.bee.client.view.grid.interceptor.AbstractGridInterceptor;
import com.butent.bee.client.view.grid.interceptor.GridInterceptor;

public class TradeDocumentItemsGrid extends AbstractGridInterceptor {

  TradeDocumentItemsGrid() {
  }

  @Override
  public GridInterceptor getInstance() {
    return new TradeDocumentItemsGrid();
  }
}
