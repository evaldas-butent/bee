package com.butent.bee.client.modules.cars;

import com.butent.bee.client.view.grid.interceptor.AbstractGridInterceptor;
import com.butent.bee.client.view.grid.interceptor.GridInterceptor;
import com.butent.bee.shared.data.IsRow;

public class CarBundleItemsGrid extends AbstractGridInterceptor {
  @Override
  public void afterDeleteRow(long rowId) {
    getGridPresenter().refresh(false, false);
    super.afterDeleteRow(rowId);
  }

  @Override
  public void afterInsertRow(IsRow result) {
    getGridPresenter().refresh(false, false);
    super.afterInsertRow(result);
  }

  @Override
  public GridInterceptor getInstance() {
    return new CarBundleItemsGrid();
  }
}
