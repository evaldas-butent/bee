package com.butent.bee.client.modules.finance;

import com.butent.bee.client.view.grid.interceptor.GridInterceptor;

class FinancialRecordsGrid extends FinanceGrid {

  FinancialRecordsGrid() {
    super();
  }

  @Override
  public GridInterceptor getInstance() {
    return new FinancialRecordsGrid();
  }
}
