package com.butent.bee.client.modules.finance.analysis;

import com.butent.bee.client.view.grid.interceptor.AbstractGridInterceptor;
import com.butent.bee.client.view.grid.interceptor.GridInterceptor;

public class BudgetEntriesGrid extends AbstractGridInterceptor {

  public BudgetEntriesGrid() {
  }

  @Override
  public GridInterceptor getInstance() {
    return new BudgetEntriesGrid();
  }
}
