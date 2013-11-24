package com.butent.bee.client.modules.transport;

import com.butent.bee.client.view.grid.AbstractGridInterceptor;
import com.butent.bee.client.view.grid.GridInterceptor;

class CargoRequestsGrid extends AbstractGridInterceptor {

  @Override
  public GridInterceptor getInstance() {
    return new CargoRequestsGrid();
  }

  CargoRequestsGrid() {
  }
}
