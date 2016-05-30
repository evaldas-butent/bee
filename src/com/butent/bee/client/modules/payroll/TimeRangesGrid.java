package com.butent.bee.client.modules.payroll;

import com.butent.bee.client.view.grid.interceptor.AbstractGridInterceptor;
import com.butent.bee.client.view.grid.interceptor.GridInterceptor;

class TimeRangesGrid extends AbstractGridInterceptor {

  TimeRangesGrid() {
  }

  @Override
  public GridInterceptor getInstance() {
    return new TimeRangesGrid();
  }
}
