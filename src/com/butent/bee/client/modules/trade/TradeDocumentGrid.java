package com.butent.bee.client.modules.trade;

import com.butent.bee.client.view.grid.interceptor.AbstractGridInterceptor;
import com.butent.bee.client.view.grid.interceptor.GridInterceptor;

public class TradeDocumentGrid extends AbstractGridInterceptor {

  TradeDocumentGrid() {
  }

  @Override
  public GridInterceptor getInstance() {
    return new TradeDocumentGrid();
  }
}
